package org.mjdev.doorbellassistant.agent.stt

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.mjdev.whisper.WhisperContext

class WhisperKit(
    private val context: Context,
    private val filesDir: File? = context.filesDir,
    private var callback: suspend (WhisperKitResult) -> Unit = { }
) {
    private var isDownloading = false
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var modelType: WhisperModelType = WhisperModelType.SMALL
    private var whisperContext: WhisperContext? = null

    fun setModel(modelType: WhisperModelType) {
        this.modelType = modelType
    }

    fun setCallback(callback: suspend (WhisperKitResult) -> Unit) {
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
        isDownloading = true
        scope.launch {
            checkAndDownloadModel { percent ->
                withContext(Dispatchers.Main) {
                    callback(WhisperKitResult.WhisperKitDownload(percent))
                }
            }
            isDownloading = false
            runCatching {
                val modelFile = File(filesDir, "models/${modelType.modelName}.bin")
                whisperContext = WhisperContext.createContextFromFile(modelFile.absolutePath)
                withContext(Dispatchers.Main) {
                    callback(WhisperKitResult.WhisperKitInitialized)
                }
            }.getOrElse { e ->
                withContext(Dispatchers.Main) {
                    callback(WhisperKitResult.WhisperKitError(e))
                }
                whisperContext = null
            }
        }
    }

    suspend fun release() {
        runCatching {
            whisperContext?.release()
            whisperContext = null
            withContext(Dispatchers.Main) {
                callback(WhisperKitResult.WhisperKitReleased)
            }
        }.onFailure { e ->
            withContext(Dispatchers.Main) {
                callback(WhisperKitResult.WhisperKitError(e))
            }
        }
    }

    suspend fun transcribe(data: ByteArray) {
        if (whisperContext == null) {
            withContext(Dispatchers.Main) {
                callback(
                    WhisperKitResult.WhisperKitError(
                        IllegalStateException("WhisperContext not initialized")
                    )
                )
            }
        } else {
            callback(WhisperKitResult.WhisperKitTranscribing)
            runCatching {
                val floatData = convertBytesToFloats(data)
                val result = whisperContext?.transcribeData(floatData, printTimestamp = false) ?: ""
                withContext(Dispatchers.Main) {
                    callback(
                        WhisperKitResult.WhisperKitText(
                            result.trim(),
                            result.trim().split(" ").toList()
                        )
                    )
                }
            }.getOrElse { e ->
                withContext(Dispatchers.Main) {
                    callback(WhisperKitResult.WhisperKitError(e))
                }
            }
        }
    }

    private fun convertBytesToFloats(data: ByteArray): FloatArray {
        val shorts = ShortArray(data.size / 2)
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return FloatArray(shorts.size) { shorts[it] / 32768.0f }
    }

    sealed class WhisperKitResult {
        object WhisperKitInitialized : WhisperKitResult()

        object WhisperKitReleased : WhisperKitResult()

        object WhisperKitTranscribing : WhisperKitResult()

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

    enum class WhisperModelType(
        val modelName: String,
        val url: String,
        val frequency: Int = 16000,
        val channels: Int = 1,
    ) {
        SMALL(
            modelName = "ggml-small",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin",
        ),
        MEDIUM(
            modelName = "ggml-medium",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin",
        ),
        LARGE(
            modelName = "ggml-large-v3",
            url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.bin",
        )
    }
}
