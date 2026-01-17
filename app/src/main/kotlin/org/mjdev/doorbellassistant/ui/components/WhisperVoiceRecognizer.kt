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
import org.mjdev.doorbellassistant.ui.components.VoiceRecognizerState.Companion.rememberWhisperVoiceRecognizerState
import org.mjdev.doorbellassistant.agent.stt.VoiceKit
import org.mjdev.doorbellassistant.agent.stt.VoiceKitListener
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.KitResult
import org.mjdev.doorbellassistant.agent.stt.transcribers.vosk.VoskKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.vosk.VoskModelType
import org.mjdev.doorbellassistant.extensions.ComposeExt.logPosition
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import kotlin.Float

class VoiceRecognizerState(
    val context: Context,
    val modelType: ITKitModel = VoskModelType.CS_SMALL,
    val onInitialized: VoiceRecognizerState.() -> Unit = {},
    val onReleased: VoiceRecognizerState.() -> Unit = {},
    var onVoiceDetected: VoiceRecognizerState.() -> Unit = {},
    var onVoiceStarts: VoiceRecognizerState.() -> Unit = {},
    val onVoiceTranscribed: VoiceRecognizerState.(
        fulltext: String,
        segments: List<String>
    ) -> Unit = { _, _ -> },
    val onVoiceEnds: VoiceRecognizerState.() -> Unit = {},
    val onDownloading: (percent: Float) -> Unit = {},
    val onFailure: VoiceRecognizerState.(e: Throwable) -> Unit = {},
    val voiceDetectionSensitivity: Float = 0.2f,
    val stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    val maxRecordingDurationMs: Long = 20000L,
    val minRecordingDurationMs: Long = 2000L,
    val onThinking: () -> Unit = {},
    val createKit: (Context) -> ITKit = { context -> VoskKit(context) },
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
                        this@VoiceRecognizerState.onInitialized(this@VoiceRecognizerState)
                    }

                    is KitResult.Text -> {
                        _isThinking.value = false
                        _isListening.value = false
                        this@VoiceRecognizerState.onVoiceTranscribed(
                            result.text,
                            result.segments
                        )
                    }

                    is KitResult.Error -> {
                        _isThinking.value = false
                        _isListening.value = false
                        this@VoiceRecognizerState.onFailure(result.error)
                    }

                    is KitResult.Transcribing -> {
                        _isThinking.value = true
                        _isListening.value = true
                        this@VoiceRecognizerState.onThinking()
                    }

                    is KitResult.Download -> {
                        _isInitialized.value = false
                        _isThinking.value = false
                        this@VoiceRecognizerState.onDownloading(result.percent)
                    }

                    is KitResult.Released -> {
                        _isInitialized.value = false
                        _isThinking.value = false
                        _isListening.value = false
                        this@VoiceRecognizerState.onReleased(this@VoiceRecognizerState)
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
            maxRecordingDurationMs = maxRecordingDurationMs,
            minRecordingDurationMs = minRecordingDurationMs,
            listener = object : VoiceKitListener {
                @RequiresPermission(Manifest.permission.RECORD_AUDIO)
                override suspend fun onVoiceDetected() {
                    _isListening.value = true
                    withContext(Dispatchers.Main) {
                        this@VoiceRecognizerState.onVoiceDetected()
                    }
                }

                override suspend fun onVoiceStarts() {
                    _isListening.value = true
                    withContext(Dispatchers.Main) {
                        this@VoiceRecognizerState.onVoiceStarts()
                    }
                }

                override suspend fun onGotVoiceChunk(data: ByteArray) {
                    _isListening.value = true
                    _isThinking.value = true
                    withContext(Dispatchers.Main) {
                        this@VoiceRecognizerState.onGotVoiceChunk(data)
                    }
                }

                override suspend fun voiceEnds() {
                    _isListening.value = false
                    _isThinking.value = false
                    withContext(Dispatchers.Main) {
                        this@VoiceRecognizerState.onVoiceEnds()
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
            modelType: ITKitModel = VoskModelType.CS_SMALL,
            onInitialized: VoiceRecognizerState.() -> Unit = {},
            onVoiceDetected: VoiceRecognizerState.() -> Unit = {},
            onVoiceStarts: VoiceRecognizerState.() -> Unit = {},
            onVoiceTranscribed: VoiceRecognizerState.(
                fulltext: String,
                segments: List<String>
            ) -> Unit = { _, _ -> },
            onVoiceEnds: VoiceRecognizerState.() -> Unit = {},
            onDownloading: (percent: Float) -> Unit = {},
            onThinking: () -> Unit = {},
            onFailure: VoiceRecognizerState.(e: Throwable) -> Unit = {},
            voiceDetectionSensitivity: Float = 0.2f,
            stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
            maxRecordingDurationMs: Long = 20000L,
            minRecordingDurationMs: Long = 2000L,
            createKit: (Context) -> ITKit = { context -> VoskKit(context) },
        ) = remember {
            VoiceRecognizerState(
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
                maxRecordingDurationMs = maxRecordingDurationMs,
                minRecordingDurationMs = minRecordingDurationMs,
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
    modelType: ITKitModel = VoskModelType.CS_SMALL,
    voiceDetectionSensitivity: Float = 0.2f,
    stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    maxRecordingDurationMs: Long = 20000L,
    minRecordingDurationMs: Long = 2000L,
    autoStart: Boolean = false,
    createKit: (Context) -> ITKit = { context -> VoskKit(context) },
    state: VoiceRecognizerState = rememberWhisperVoiceRecognizerState(
        context = context,
        modelType = modelType,
        voiceDetectionSensitivity = voiceDetectionSensitivity,
        stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
        maxRecordingDurationMs = maxRecordingDurationMs,
        minRecordingDurationMs = minRecordingDurationMs,
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
