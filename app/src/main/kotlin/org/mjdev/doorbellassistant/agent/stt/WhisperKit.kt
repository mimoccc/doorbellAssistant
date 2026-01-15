package org.mjdev.doorbellassistant.agent.stt

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class WhisperKit(
    private val context: Context,
    private val filesDir: File? = context.filesDir,
    private var callback: suspend (WhisperKitResult) -> Unit = { }
) {
    private var isDownloading = false
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var modelType: WhisperModelType = WhisperModelType.SMALL

    fun setModel(modelType: WhisperModelType) {
        this.modelType = modelType
    }

    fun setCallback(callback: suspend (WhisperKitResult) -> Unit) {
        this.callback = callback
    }

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
                    val connection = url.openConnection() as java.net.HttpURLConnection
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
                callback(WhisperKitResult.WhisperKitError(e))
            }
        }
    }

    fun init() {
        scope.launch {
            checkAndDownloadModel { percent ->
                withContext(Dispatchers.Main) {
                    callback(WhisperKitResult.WhisperKitDownload(percent))
                }
            }
            while (isDownloading) {
                delay(100)
            }
            withContext(Dispatchers.Main) {
                callback(WhisperKitResult.WhisperKitInitialized)
            }
        }
    }

    suspend fun release() {
        // todo
        withContext(Dispatchers.Main) {
            callback(WhisperKitResult.WhisperKitReleased)
        }
    }

    @Suppress("unused")
    suspend fun transcribe(data: ByteArray) {
        // todo
        withContext(Dispatchers.Main) {
            callback.invoke(WhisperKitResult.WhisperKitText("", listOf()))
        }
    }

    sealed class WhisperKitResult {
        object WhisperKitInitialized : WhisperKitResult()
        object WhisperKitReleased : WhisperKitResult()
        data class WhisperKitError(
            val error: Throwable
        ) : WhisperKitResult()

        data class WhisperKitDownload(
            val percent: Float
        ) : WhisperKitResult()

        data class WhisperKitText(
            val text: String = "",
            val segments: List<String> = listOf(),
        ) : WhisperKitResult()
    }

    @Suppress("unused")
    enum class WhisperModelType(
        val modelName: String,
        val url: String,
        val frequency: Int = 16000,
        val channels: Int = 1,
        val maxDuration: Long = 20000,
    ) {
        SMALL(
            modelName = "whisper-small-multilingual",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin",
        ),
        MEDIUM(
            modelName = "whisper-medium-multilingual",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin",
        ),
        LARGE(
            modelName = "whisper-large-v3",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.bin",
        )
    }
}
