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
import android.util.Log
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

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class WhisperKit(
    private val context: Context,
    private val filesDir: File? = context.filesDir,
    private val whisperContext: CloseableCoroutineDispatcher = newSingleThreadContext(WhisperKit::class.simpleName!!),
    private val whisperScope: CoroutineScope = CoroutineScope(whisperContext),
) : ITKit(
    context,
    whisperContext,
    whisperScope
) {
    private var isDownloading = false
    private var modelType: WhisperModelType = WhisperModelType.MEDIUM
    private var whisperLibContext: WhisperContext? = null
    private val modelFile
        get() = filesDir!!.resolve("whisper").apply {
            mkdirs()
        }.resolve(modelType.fileName).apply {
            Log.d(TAG, "Whisper file path: $absolutePath")
        }

    override fun setModel(modelType: ITKitModel) {
        this.modelType = modelType as? WhisperModelType
            ?: WhisperModelType.MEDIUM
    }

    private suspend fun checkAndDownloadModel(
        onDownloading: (percent: Float) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Checking model existence.")
        Log.d(TAG, "Path: ${modelFile.absolutePath}")
        runCatching {
            if (!modelFile.exists()) {
                modelFile.parentFile?.mkdirs()
                val assetExists = runCatching {
                    context.assets.open("whisper/${modelType.modelName}.bin").close()
                    true
                }.getOrDefault(false)
                Log.d(TAG, "Is in assets: $assetExists")
                if (assetExists) {
                    Log.d(TAG, "Copying from assets...")
                    context.assets.open("whisper/${modelType.modelName}.bin").use { input ->
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
                    Log.d(TAG, "Not in assets, downloading...")
                    isDownloading = true
                    downloadToFile(
                        url = modelType.url,
                        file = modelFile,
                        onProgress = { _, p ->
                            onDownloading(p)
                            Log.d(TAG, "Download progress: ${p * 100}")
                        },
                        onComplete = { success, f ->
                            Log.d(TAG, "File downloaded: ${f?.absolutePath}")
                            isDownloading = !success
                        }
                    )
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
                whisperLibContext = WhisperContext.createContextFromFile(
                    modelFile.absolutePath
                )
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

    companion object {
        private val TAG = WhisperKit::class.simpleName
    }
}
