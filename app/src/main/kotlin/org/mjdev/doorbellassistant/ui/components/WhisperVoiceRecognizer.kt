package org.mjdev.doorbellassistant.ui.components

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.doorbellassistant.agent.stt.TextOutputCallback
import org.mjdev.doorbellassistant.ui.components.WhisperRecognizerState.Companion.rememberOboeRecognizerState
import org.mjdev.doorbellassistant.agent.stt.VoiceKit
import org.mjdev.doorbellassistant.agent.stt.VoiceKitListener
import org.mjdev.doorbellassistant.agent.stt.WhisperKit
import org.mjdev.doorbellassistant.agent.stt.WhisperModelType
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Suppress("RedundantSuspendModifier")
class WhisperRecognizerState(
    val context: Context,
    val filesDir: File? = context.filesDir,
    val modelType: WhisperModelType = WhisperModelType.MEDIUM,
    val onInitialized: WhisperRecognizerState.() -> Unit = {},
    var onVoiceDetected: WhisperRecognizerState.() -> Unit = {},
    var onVoiceStarts: WhisperRecognizerState.() -> Unit = {},
    val onVoiceTranscribed: WhisperRecognizerState.(
        fulltext: String,
        segments: List<String>
    ) -> Unit = { _, _ -> },
    val onVoiceEnds: WhisperRecognizerState.() -> Unit = {},
    val onDownloading: (percent: Float) -> Unit = {},
    val onFailure: WhisperRecognizerState.(e: Throwable) -> Unit = {},
) {
    @Volatile
    var isListening: Boolean = false
        internal set

    private val whisperKit by lazy {
        WhisperKit(context).apply {
            setModel(modelType.modelName)
            setCallback { what, result ->
                when (what) {
                    TextOutputCallback.MSG_INIT -> {
                        this@WhisperRecognizerState.onInitialized(this@WhisperRecognizerState)
                    }

                    TextOutputCallback.MSG_TEXT_OUT -> {
                        val fullText = result.text
                        val segments = result.segments
                        this@WhisperRecognizerState.onVoiceTranscribed(
                            fullText,
                            segments
                        )
                    }

                    TextOutputCallback.MSG_CLOSE -> {
                        // Cleanup complete
                    }
                }
            }
        }
    }
    private val voiceKit by lazy {
        VoiceKit(
            context = context,
            listener = object : VoiceKitListener {
                @RequiresPermission(Manifest.permission.RECORD_AUDIO)
                override suspend fun onVoiceDetected() {
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onVoiceDetected()
                    }
                    startRecording()
                }

                override suspend fun onVoiceStarts() {
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onVoiceStarts()
                    }
                }

                override suspend fun onGotVoiceChunk(data: ByteArray) {
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onGotVoiceChunk(data)
                    }
                }

                override suspend fun voiceEnds() {
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onVoiceEnds()
                    }
                }
            }
        )
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend fun startListen() = runCatching {
        if (!isListening) {
            checkAndDownloadModel(modelType) { percent ->
                onDownloading(percent)
            }
            whisperKit.init(
                modelType.frequency,
                modelType.channels,
                modelType.duration
            )
            voiceKit.start(modelType.frequency, modelType.channels)
            isListening = true
        }
    }.onFailure { e ->
        withContext(Dispatchers.Main) {
            onFailure(e)
        }
    }

    suspend fun stopListening() = runCatching {
        if (isListening) {
            isListening = false
            voiceKit.stop()
            whisperKit.release()
        }
    }.onFailure { e ->
        withContext(Dispatchers.Main) {
            onFailure(e)
        }
    }

    private suspend fun checkAndDownloadModel(
        modelType: WhisperModelType,
        onDownloading: (percent: Float) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
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
    }

    private suspend fun onGotVoiceChunk(data: ByteArray) {
        whisperKit.transcribe(data)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private suspend fun startRecording() {
        voiceKit.startRecording()
    }

    companion object {
        @Suppress("ParamsComparedByRef")
        @Composable
        fun rememberOboeRecognizerState(
            context: Context = LocalContext.current,
            filesDir: File? = context.filesDir,
            modelType: WhisperModelType = WhisperModelType.MEDIUM,
            onInitialized: WhisperRecognizerState.() -> Unit = {},
            onVoiceDetected: WhisperRecognizerState.() -> Unit = {},
            onVoiceStarts: WhisperRecognizerState.() -> Unit = {},
            onVoiceTranscribed: WhisperRecognizerState.(
                fulltext: String,
                segments: List<String>
            ) -> Unit = { _, _ -> },
            onVoiceEnds: WhisperRecognizerState.() -> Unit = {},
            onDownloading: (percent: Float) -> Unit = {},
            onFailure: WhisperRecognizerState.(e: Throwable) -> Unit = {},
        ) = remember {
            WhisperRecognizerState(
                context = context,
                filesDir = filesDir,
                modelType = modelType,
                onInitialized = onInitialized,
                onVoiceDetected = onVoiceDetected,
                onVoiceStarts = onVoiceStarts,
                onVoiceTranscribed = onVoiceTranscribed,
                onVoiceEnds = onVoiceEnds,
                onDownloading = onDownloading,
                onFailure = onFailure
            )
        }
    }
}

@Previews
@Composable
fun WhisperVoiceRecognizer(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    filesDir: File? = context.filesDir,
    modelType: WhisperModelType = WhisperModelType.MEDIUM,
    state: WhisperRecognizerState = rememberOboeRecognizerState(
        context = context,
        filesDir = filesDir,
        modelType = modelType,
    )
) = PhoneTheme {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    scope.launch {
                        state.stopListening()
                    }
                }

                Lifecycle.Event.ON_DESTROY -> {
                    scope.launch {
                        state.stopListening()
                    }
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            scope.launch {
                state.stopListening()
            }
        }
    }
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (state.isListening) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = if (state.isListening) "Listening" else "Not listening",
            tint = if (state.isListening) Color.Red else Color.Gray,
            modifier = Modifier.size(32.dp)
        )
    }
}
