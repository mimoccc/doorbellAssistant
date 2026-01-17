package org.mjdev.doorbellassistant.agent.stt.transcribers.vosk

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.KitResult
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class VoskKit(
    private val context: Context,
    private val filesDir: File? = context.filesDir,
    private var callback: suspend (KitResult) -> Unit = { }
) : ITKit {
    private var isDownloading = false
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var modelType: VoskModelType = VoskModelType.CS_SMALL
    private var model: Model? = null
    private var recognizer: Recognizer? = null

    override fun setModel(modelType: ITKitModel) {
        this.modelType = modelType as? VoskModelType
            ?: throw IllegalArgumentException("Invalid model type")
    }

    override fun setCallback(callback: suspend (KitResult) -> Unit) {
        this.callback = callback
    }

    private fun modelDir(): File = File(
        filesDir,
        "vosk/${modelType.lang.id}/${modelType.size.id}"
    )

    private fun modelMarkerFile(dir: File): File = File(dir, ".ready")

    private suspend fun checkAndDownloadModel(
        onDownloading: suspend (percent: Float) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val dir = modelDir()
            val marker = modelMarkerFile(dir)
            if (marker.exists().not()) {
                dir.mkdirs()
                val assetName = modelType.url.substringAfterLast("/")
                val zipFile = File(dir, assetName)
                isDownloading = true
                val assetExists = context.assets.list(modelType.assetsFolder)?.contains(assetName) ?: false
                if (assetExists) {
                    context.assets.open("${modelType.assetsFolder}/$assetName").use { input ->
                        FileOutputStream(zipFile).use { input.copyTo(it) }
                    }
                } else {
                    downloadToFile(
                        url = modelType.url,
                        dest = zipFile,
                        onProgress = onDownloading
                    )
                }
                unzip(zipFile, dir)
                val actualModelPath = findActualModelRoot(dir)
                    ?: throw IllegalStateException("Unzipped model root not found in ${dir.absolutePath}")
                marker.writeText(actualModelPath.absolutePath)
                onDownloading(1f)
            }
        }.onFailure { e ->
            isDownloading = false
            withContext(Dispatchers.Main) { callback(KitResult.Error(e)) }
        }.onSuccess {
            isDownloading = false
        }
    }

    override fun init() {
        scope.launch {
            checkAndDownloadModel { percent ->
                withContext(Dispatchers.Main) { callback(KitResult.Download(percent)) }
            }
            while (isDownloading) delay(100)
            withContext(Dispatchers.IO) {
                recognizer?.close()
                recognizer = null
                model?.close()
                model = null
                val dir = modelDir()
                val marker = modelMarkerFile(dir)
                val actualModelPath = marker.takeIf { it.exists() }?.readText()?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { File(it) }
                    ?: findActualModelRoot(dir)
                    ?: throw IllegalStateException("Model not ready: ${dir.absolutePath}")
                model = Model(actualModelPath.absolutePath)
                recognizer = Recognizer(model, modelType.frequency.toFloat())
            }
            withContext(Dispatchers.Main) { callback(KitResult.Initialized) }
        }
    }

    override fun release() {
        scope.launch {
            withContext(Dispatchers.IO) {
                recognizer?.close()
                recognizer = null
                model?.close()
                model = null
            }
            withContext(Dispatchers.Main) { callback(KitResult.Released) }
        }
    }

    @Suppress("unused")
    override fun transcribe(data: ByteArray) {
        scope.launch {
            val rec = recognizer
            if (rec == null) {
                withContext(Dispatchers.Main) {
                    callback(KitResult.Error(IllegalStateException("Vosk not initialized")))
                }
            } else {
                withContext(Dispatchers.Main) { callback(KitResult.Transcribing) }
                val accepted = withContext(Dispatchers.IO) { rec.acceptWaveForm(data, data.size) }
                val json =
                    withContext(Dispatchers.IO) { if (accepted) rec.result else rec.partialResult }
                val obj = runCatching { JSONObject(json) }.getOrNull()
                val text = (obj?.optString("text") ?: obj?.optString("partial") ?: "").trim()
                val segments = if (text.isBlank()) emptyList() else listOf(text)
                withContext(Dispatchers.Main) { callback(KitResult.Text(text, segments)) }
            }
        }
    }

    @Suppress("unused")
    private suspend fun downloadToFile(
        url: String,
        dest: File,
        onProgress: suspend (Float) -> Unit,
    ) = withContext(Dispatchers.IO) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 60_000
            instanceFollowRedirects = true
        }
        conn.connect()
        val total = conn.contentLengthLong.takeIf { it > 0L } ?: -1L
        conn.inputStream.use { input ->
            FileOutputStream(dest).use { output ->
                val buffer = ByteArray(64 * 1024)
                var copied = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    copied += read
                    if (total > 0L) onProgress(
                        (copied.toFloat() / total.toFloat()).coerceIn(
                            0f,
                            1f
                        )
                    )
                }
            }
        }
        conn.disconnect()
    }

    private fun unzip(
        zipFile: File,
        targetDir: File
    ) {
        ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                val outFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { out ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val read = zis.read(buffer)
                            if (read < 0) break
                            out.write(buffer, 0, read)
                        }
                    }
                }
                zis.closeEntry()
            }
        }
    }

    private fun findActualModelRoot(
        baseDir: File
    ): File? = baseDir.listFiles()?.toList().orEmpty().let { children ->
        children.firstOrNull {
            it.isDirectory && (File(it, "am").exists() || File(it, "conf").exists())
        } ?: children.filter { it.isDirectory }.firstNotNullOfOrNull { dir ->
            dir.listFiles()?.firstOrNull {
                it.isDirectory && (File(it, "am").exists() || File(it, "conf").exists())
            }
        }
    }
}
