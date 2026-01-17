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
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.KitResult
import org.mjdev.doorbellassistant.agent.stt.transcribers.whisper.WhisperKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.whisper.WhisperModelType
import org.mjdev.doorbellassistant.extensions.ComposeExt.logPosition
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import java.io.File
import kotlin.Float

class WhisperRecognizerState(
    val context: Context,
    val modelType: ITKitModel = WhisperModelType.SMALL,
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
    val onThinking: () -> Unit = {},
    val createKit: (Context) -> ITKit = { context -> WhisperKit(context) },
) {
    private var _isThinking: MutableState<Boolean> = mutableStateOf(false)
    private var _isInitialized: MutableState<Boolean> = mutableStateOf(false)
    private var _isListeningStarted: MutableState<Boolean> = mutableStateOf(false)
    private var _isListening: MutableState<Boolean> = mutableStateOf(false)
    private val scope = CoroutineScope(Dispatchers.Default)

    val isListening
        get() = _isListening.value || _isListeningStarted.value
    val isInitialized
        get() = _isInitialized.value
    val isThinking
        get() = _isThinking.value

    private val transcribeKit by lazy {
        createKit(context).apply {
            setModel(modelType)
            setCallback { result ->
                when (result) {
                    is KitResult.Initialized -> {
                        _isInitialized.value = true
                        _isThinking.value = false
                        _isListening.value = false
                        this@WhisperRecognizerState.onInitialized(this@WhisperRecognizerState)
                    }

                    is KitResult.Text -> {
                        _isThinking.value = false
                        _isListening.value = false
                        this@WhisperRecognizerState.onVoiceTranscribed(
                            result.text,
                            result.segments
                        )
                    }

                    is KitResult.Error -> {
                        _isThinking.value = false
                        _isListening.value = false
                        this@WhisperRecognizerState.onFailure(result.error)
                    }

                    is KitResult.Transcribing -> {
                        _isThinking.value = true
                        _isListening.value = true
                        this@WhisperRecognizerState.onThinking()
                    }

                    is KitResult.Download -> {
                        _isInitialized.value = false
                        _isThinking.value = false
                        this@WhisperRecognizerState.onDownloading(result.percent)
                    }

                    is KitResult.Released -> {
                        _isInitialized.value = false
                        _isThinking.value = false
                        _isListening.value = false
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
                    _isThinking.value = false
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onVoiceDetected()
                    }
                }

                override suspend fun onVoiceStarts() {
                    _isListening.value = true
                    _isThinking.value = false
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onVoiceStarts()
                    }
                }

                override suspend fun onGotVoiceChunk(data: ByteArray) {
                    _isListening.value = true
                    _isThinking.value = false
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onGotVoiceChunk(data)
                    }
                }

                override suspend fun voiceEnds() {
                    _isListening.value = false
                    _isThinking.value = false
                    withContext(Dispatchers.Main) {
                        this@WhisperRecognizerState.onVoiceEnds()
                    }
                }
            }
        )
    }

    init {
        transcribeKit.init()
    }

    @SuppressLint("MissingPermission")
    fun startListen() = scope.launch {
        if (!_isListeningStarted.value) {
            voiceKit.start(modelType.frequency, modelType.channels)
            _isListeningStarted.value = true
        }
    }

    fun stopListening() = runCatching {
        _isListeningStarted.value = false
        scope.launch {
            voiceKit.stop()
            transcribeKit.release()
        }
    }.onFailure { e ->
        onFailure(e)
    }

    private suspend fun onGotVoiceChunk(data: ByteArray) {
        runCatching {
            transcribeKit.transcribe(data)
        }.onFailure { e ->
            withContext(Dispatchers.Main) {
                onFailure(e)
            }
        }
    }

    companion object {
        @Suppress("ParamsComparedByRef")
        @Composable
        fun rememberWhisperVoiceRecognizerState(
            context: Context = LocalContext.current,
            modelType: ITKitModel = WhisperModelType.SMALL,
            onInitialized: WhisperRecognizerState.() -> Unit = {},
            onVoiceDetected: WhisperRecognizerState.() -> Unit = {},
            onVoiceStarts: WhisperRecognizerState.() -> Unit = {},
            onVoiceTranscribed: WhisperRecognizerState.(
                fulltext: String,
                segments: List<String>
            ) -> Unit = { _, _ -> },
            onVoiceEnds: WhisperRecognizerState.() -> Unit = {},
            onDownloading: (percent: Float) -> Unit = {},
            onThinking: () -> Unit = {},
            onFailure: WhisperRecognizerState.(e: Throwable) -> Unit = {},
            voiceDetectionSensitivity: Float = 0.2f,
            stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
            createKit: (Context) -> ITKit = { context -> WhisperKit(context) },
        ) = remember {
            WhisperRecognizerState(
                context = context,
                modelType = modelType,
                onInitialized = onInitialized,
                onVoiceDetected = onVoiceDetected,
                onVoiceStarts = onVoiceStarts,
                onVoiceTranscribed = onVoiceTranscribed,
                onVoiceEnds = onVoiceEnds,
                onDownloading = onDownloading,
                onThinking = onThinking,
                onFailure = onFailure,
                voiceDetectionSensitivity = voiceDetectionSensitivity,
                stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
                createKit = createKit,
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
    modelType: ITKitModel = WhisperModelType.SMALL,
    voiceDetectionSensitivity: Float = 0.2f,
    stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    autoStart: Boolean = false,
    createKit: (Context) -> ITKit = { context -> WhisperKit(context) },
    state: WhisperRecognizerState = rememberWhisperVoiceRecognizerState(
        context = context,
        modelType = modelType,
        voiceDetectionSensitivity = voiceDetectionSensitivity,
        stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
        createKit = createKit,
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
