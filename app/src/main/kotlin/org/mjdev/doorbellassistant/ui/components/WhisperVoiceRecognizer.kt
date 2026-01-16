package org.mjdev.doorbellassistant.ui.components

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.doorbellassistant.ui.components.WhisperRecognizerState.Companion.rememberWhisperVoiceRecognizerState
import org.mjdev.doorbellassistant.agent.stt.VoiceKit
import org.mjdev.doorbellassistant.agent.stt.VoiceKitListener
import org.mjdev.doorbellassistant.agent.stt.WhisperKit
import org.mjdev.doorbellassistant.extensions.ComposeExt.logPosition
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import java.io.File
import kotlin.Float

class WhisperRecognizerState(
    val context: Context,
    val filesDir: File? = context.filesDir,
    val modelType: WhisperKit.WhisperModelType = WhisperKit.WhisperModelType.MEDIUM,
    val onInitialized: WhisperRecognizerState.() -> Unit = {},
    val onReleased: WhisperRecognizerState.() -> Unit = {},
    var onVoiceDetected: WhisperRecognizerState.() -> Unit = {},
    var onVoiceStarts: WhisperRecognizerState.() -> Unit = {},
    val onVoiceTranscribed: WhisperRecognizerState.(
        fulltext: String,
        segments: List<String>
    ) -> Unit = { _, _ -> },
    val onVoiceEnds: WhisperRecognizerState.() -> Unit = {},
    val onDownloading: (percent: Float) -> Unit = {},
    val onFailure: WhisperRecognizerState.(e: Throwable) -> Unit = {},
    val voiceDetectionSensitivity: Float = 0.2f,
    val stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
) {
    private var _isInitialized : Boolean = false
    private var _isListeningStarted = false
    private var _isListening: MutableState<Boolean> = mutableStateOf(false)
    private val scope = CoroutineScope(Dispatchers.Default)

    val isListening
        get() = _isListening.value
    val isInitialized
        get() = _isInitialized

    private val whisperKit by lazy {
        WhisperKit(context, filesDir).apply {
            setModel(modelType)
            setCallback { result ->
                when (result) {
                    is WhisperKit.WhisperKitResult.WhisperKitInitialized -> {
                        _isInitialized = true
                        this@WhisperRecognizerState.onInitialized(this@WhisperRecognizerState)
                    }

                    is WhisperKit.WhisperKitResult.WhisperKitText -> {
                        this@WhisperRecognizerState.onVoiceTranscribed(
                            result.text,
                            result.segments
                        )
                    }

                    is WhisperKit.WhisperKitResult.WhisperKitError -> {
                        _isInitialized = false
                        this@WhisperRecognizerState.onFailure(result.error)
                    }

                    is WhisperKit.WhisperKitResult.WhisperKitDownload -> {
                        _isInitialized = false
                        this@WhisperRecognizerState.onDownloading(result.percent)
                    }

                    WhisperKit.WhisperKitResult.WhisperKitReleased -> {
                        _isInitialized = false
                        this@WhisperRecognizerState.onReleased(this@WhisperRecognizerState)
                    }
                }
            }
        }
    }
    private val voiceKit by lazy {
        VoiceKit(
            context = context,
            voiceDetectionSensitivity = voiceDetectionSensitivity,
            stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
            listener = object : VoiceKitListener {
                @RequiresPermission(Manifest.permission.RECORD_AUDIO)
                override suspend fun onVoiceDetected() {
                    _isListening.value = true
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onVoiceDetected()
                    }
                }

                override suspend fun onVoiceStarts() {
                    _isListening.value = true
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onVoiceStarts()
                    }
                }

                override suspend fun onGotVoiceChunk(data: ByteArray) {
                    _isListening.value = true
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onGotVoiceChunk(data)
                    }
                }

                override suspend fun voiceEnds() {
                    _isListening.value = false
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onVoiceEnds()
                    }
                }
            }
        )
    }

    init {
        whisperKit.init()
    }

    @SuppressLint("MissingPermission")
    fun startListen() = scope.launch {
        if (!_isListeningStarted) {
            voiceKit.start(modelType.frequency, modelType.channels)
            _isListeningStarted = true
        }
    }

    fun stopListening() = runCatching {
        _isListeningStarted = false
        scope.launch {
            voiceKit.stop()
            whisperKit.release()
        }
    }.onFailure { e ->
        onFailure(e)
    }

    private suspend fun onGotVoiceChunk(data: ByteArray) {
        whisperKit.transcribe(data)
    }

//    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
//    private suspend fun startRecording() {
//        voiceKit.startRecording()
//    }

    companion object {
        @Suppress("ParamsComparedByRef")
        @Composable
        fun rememberWhisperVoiceRecognizerState(
            context: Context = LocalContext.current,
            filesDir: File? = context.filesDir,
            modelType: WhisperKit.WhisperModelType = WhisperKit.WhisperModelType.MEDIUM,
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
            voiceDetectionSensitivity: Float = 0.2f,
            stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
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
                onFailure = onFailure,
                voiceDetectionSensitivity = voiceDetectionSensitivity,
                stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
            )
        }
    }
}

@Suppress("ParamsComparedByRef")
@Previews
@Composable
fun WhisperVoiceRecognizer(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    filesDir: File? = context.filesDir,
    modelType: WhisperKit.WhisperModelType = WhisperKit.WhisperModelType.MEDIUM,
    voiceDetectionSensitivity: Float = 0.2f,
    stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    autoStart: Boolean = false,
    state: WhisperRecognizerState = rememberWhisperVoiceRecognizerState(
        context = context,
        filesDir = filesDir,
        modelType = modelType,
        voiceDetectionSensitivity = voiceDetectionSensitivity,
        stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
    )
) = PhoneTheme {
    logPosition()
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (state.isListening) Icons.Default.Mic
            else Icons.Default.MicOff,
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
    DisposableEffect(state) {
        if (autoStart) {
            state.startListen()
        }
        onDispose {
            state.stopListening()
        }
    }
}
