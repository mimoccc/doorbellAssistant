package org.mjdev.doorbellassistant.agent.stt.transcribers.whisper

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.KitResult
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.mjdev.whisper.WhisperContext

class WhisperKit(
    private val context: Context,
    private val filesDir: File? = context.filesDir,
    private var callback: suspend (KitResult) -> Unit = { }
) : ITKit {
    private var isDownloading = false
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var modelType: WhisperModelType = WhisperModelType.SMALL
    private var whisperContext: WhisperContext? = null

    override fun setModel(modelType: ITKitModel) {
        this.modelType = modelType as? WhisperModelType
            ?: throw IllegalArgumentException("Invalid model type")
    }

    override fun setCallback(callback: suspend (KitResult) -> Unit) {
        this.callback = callback
    }

    // todo check integrity
    private suspend fun checkAndDownloadModel(
        onDownloading: suspend (percent: Float) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val modelFile = File(filesDir, "models/${modelType.modelName}.bin")
            if (!modelFile.exists()) {
                modelFile.parentFile?.mkdirs()
                val assetExists = runCatching {
                    context.assets.open("models/${modelType.modelName}.bin").close()
                    true
                }.getOrDefault(false)
                if (assetExists) {
                    context.assets.open("models/${modelType.modelName}.bin").use { input ->
                        FileOutputStream(modelFile).use { output ->
                            val totalSize = input.available().toLong()
                            var bytesCopied = 0L
                            val buffer = ByteArray(8192)
                            var bytes = input.read(buffer)
                            while (bytes >= 0) {
                                output.write(buffer, 0, bytes)
                                bytesCopied += bytes
                                val progress =
                                    (bytesCopied.toFloat() / totalSize.toFloat()).coerceIn(0f, 1f)
                                onDownloading(progress)
                                bytes = input.read(buffer)
                            }
                        }
                    }
                } else if (modelType.url.isNotBlank()) {
                    val url = URL(modelType.url)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    val totalSize = connection.contentLength.toLong()
                    connection.inputStream.use { input ->
                        FileOutputStream(modelFile).use { output ->
                            var bytesCopied = 0L
                            val buffer = ByteArray(8192)
                            var bytes = input.read(buffer)
                            while (bytes >= 0) {
                                output.write(buffer, 0, bytes)
                                bytesCopied += bytes
                                val progress = if (totalSize > 0) {
                                    (bytesCopied.toFloat() / totalSize.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    0f
                                }
                                onDownloading(progress)
                                bytes = input.read(buffer)
                            }
                        }
                    }
                    connection.disconnect()
                } else {
                    throw IllegalStateException("Model ${modelType.modelName} not found in assets and no download URL provided")
                }
                onDownloading(1f)
            }
        }.onFailure { e ->
            withContext(Dispatchers.Main) {
                callback(KitResult.Error(e))
            }
        }
    }

    override fun init() {
        isDownloading = true
        scope.launch {
            checkAndDownloadModel { percent ->
                withContext(Dispatchers.Main) {
                    callback(KitResult.Download(percent))
                }
            }
            isDownloading = false
            runCatching {
                val modelFile = File(filesDir, "models/${modelType.modelName}.bin")
                whisperContext = WhisperContext.createContextFromFile(modelFile.absolutePath)
                withContext(Dispatchers.Main) {
                    callback(KitResult.Initialized)
                }
            }.getOrElse { e ->
                withContext(Dispatchers.Main) {
                    callback(KitResult.Error(e))
                }
                whisperContext = null
            }
        }
    }

    override fun release() {
        scope.launch {
            runCatching {
                whisperContext?.release()
                whisperContext = null
                withContext(Dispatchers.Main) {
                    callback(KitResult.Released)
                }
            }.onFailure { e ->
                withContext(Dispatchers.Main) {
                    callback(KitResult.Error(e))
                }
            }
        }
    }

    override fun transcribe(data: ByteArray) {
        scope.launch {
            if (whisperContext == null) {
                withContext(Dispatchers.Main) {
                    callback(KitResult.Error(IllegalStateException("WhisperContext not initialized")))
                }
            } else {
                callback(KitResult.Transcribing)
                runCatching {
                    val floatData = convertBytesToFloats(data)
                    val result =
                        whisperContext?.transcribeData(floatData, printTimestamp = false) ?: ""
                    withContext(Dispatchers.Main) {
                        callback(
                            KitResult.Text(
                                result.trim(),
                                result.trim().split(" ").toList()
                            )
                        )
                    }
                }.getOrElse { e ->
                    withContext(Dispatchers.Main) {
                        callback(KitResult.Error(e))
                    }
                }
            }
        }
    }

    private fun convertBytesToFloats(data: ByteArray): FloatArray {
        val shorts = ShortArray(data.size / 2)
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return FloatArray(shorts.size) { shorts[it] / 32768.0f }
    }
}
