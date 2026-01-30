/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.stt.transcribers.whisper

import android.content.Context
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitResult
import org.mjdev.doorbellassistant.agent.tts.PiperTTSEngine.Companion.toFloatArray
import org.mjdev.phone.helpers.DataBus
import org.mjdev.whisper.WhisperContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class WhisperKit(
    private val context: Context,
    private val filesDir: File? = context.filesDir,
    private val whisperContext: CloseableCoroutineDispatcher = newSingleThreadContext(WhisperKit::class.simpleName!!),
    private val whisperScope: CoroutineScope = CoroutineScope(whisperContext),
) : DataBus<ITKitResult>(
    scopeContext = whisperContext,
    scope = whisperScope
), ITKit {
    private var isDownloading = false
    private var modelType: WhisperModelType = WhisperModelType.MEDIUM
    private var whisperLibContext: WhisperContext? = null

    private val modelFile
        get() = File(filesDir, "models/${modelType.modelName}.bin")

    override fun setModel(modelType: ITKitModel) {
        this.modelType = modelType as? WhisperModelType
            ?: WhisperModelType.MEDIUM
    }

    private suspend fun checkAndDownloadModel(
        onDownloading: suspend (percent: Float) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        runCatching {
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
            send(ITKitResult.Error(e))
        }
    }

    override fun init() {
        whisperScope.launch {
            runCatching {
                checkAndDownloadModel { percent ->
                    send(ITKitResult.Download(percent))
                }
                while (isDownloading) {
                    delay(100)
                }
                whisperLibContext = WhisperContext.createContextFromFile(modelFile.absolutePath)
                send(ITKitResult.Initialized)
            }.onFailure { e ->
                send(ITKitResult.Error(e))
            }
        }
    }

    override fun release() {
        whisperScope.launch {
            runCatching {
                whisperLibContext?.release()
                whisperScope.launch {
                    send(ITKitResult.Released)
                }
            }.onFailure { e ->
                send(ITKitResult.Error(e))
            }
        }
    }

    override fun transcribe(data: ByteArray) {
        whisperScope.launch {
            runCatching {
                whisperLibContext?.transcribeData(data.toFloatArray(), false)
                send(ITKitResult.Transcribing)
                send(ITKitResult.Text("", listOf()))
            }.onFailure { e ->
                send(ITKitResult.Error(e))
            }
        }
    }

    override fun subscribe(
        onError: (Throwable) -> Unit,
        onEvent: (ITKitResult) -> Unit
    ) {
        (this as DataBus<*>).subscribe(onError) { ev ->
            onEvent(ev as ITKitResult)
        }
    }
}
