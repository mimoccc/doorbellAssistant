/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.service

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitResult
import org.mjdev.doorbellassistant.agent.stt.transcribers.whisper.WhisperKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.whisper.WhisperModelType
import org.mjdev.doorbellassistant.agent.stt.voice.VoiceKit
import org.mjdev.doorbellassistant.agent.stt.voice.VoiceKitResult
import org.mjdev.phone.helpers.DataBus.Companion.subscribe
import org.mjdev.phone.helpers.json.Serializable
import org.mjdev.phone.service.RemoteBindableService
import org.mjdev.phone.service.ServiceCommand
import org.mjdev.phone.service.ServiceEvent

@OptIn(ExperimentalCoroutinesApi::class)
class STTService(
    var modelType: ITKitModel = WhisperModelType.SMALL,
    val createKit: Context.() -> ITKit = {
        WhisperKit(applicationContext)
    },
    var voiceDetectionSensitivity: Float = 0.2f,
    var stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    var maxRecordingDurationMs: Long = 20000L,
    var minRecordingDurationMs: Long = 2000L,
) : RemoteBindableService() {
    val scope = CoroutineScope(Dispatchers.IO)
    val voiceInitialised = mutableStateOf(false)
    val transcriptInitialised = mutableStateOf(false)
    val transcribing = mutableStateOf(false)
    val transcribedText = mutableStateOf("")
    val voiceDetected = mutableStateOf(false)
    val lastError = mutableStateOf<Throwable?>(null)

    private val transcribeKit by lazy { createKit(baseContext).apply {
        setModel(modelType)
        subscribe { event ->
            Log.d(TAG, "$event")
            when (event) {
                is ITKitResult.Initialized -> {
                    transcriptInitialised.value = true
                }

                is ITKitResult.Text -> {
                    transcriptInitialised.value = true
                    transcribedText.value = event.text
                    transcribing.value = false
                }

                is ITKitResult.Error -> {
                    lastError.value = event.error
                }

                is ITKitResult.Transcribing -> {
                    transcriptInitialised.value = true
                    transcribing.value = true
                }

                is ITKitResult.Download -> {
                    transcriptInitialised.value = false
                }

                is ITKitResult.Released -> {
                    transcriptInitialised.value = false
                }
            }
        }
    }}
    private val voiceKit by lazy {
        VoiceKit(
            context = applicationContext,
            voiceDetectionSensitivity = voiceDetectionSensitivity,
            stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
            maxRecordingDurationMs = maxRecordingDurationMs,
            minRecordingDurationMs = minRecordingDurationMs,
        ) {
            subscribe { event ->
                Log.d(TAG, "$event")
                when (event) {
                    is VoiceKitResult.Error -> {
                        lastError.value = event.error
                    }

                    is VoiceKitResult.Initialized -> {
                        voiceInitialised.value = true
                    }

                    is VoiceKitResult.Released -> {
                        voiceInitialised.value = false
                    }

                    is VoiceKitResult.StartRecording -> {
                        voiceDetected.value = true
                    }

                    is VoiceKitResult.VoiceDetected -> {
                        voiceDetected.value = true
                    }

                    is VoiceKitResult.VoiceLost -> {
                        voiceDetected.value = false
                    }

                    is VoiceKitResult.OnVoiceRecordChunk -> {
                        transcribeKit.transcribe(event.data)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        voiceKit.init()
        transcribeKit.init()
    }

    override fun onDestroy() {
        super.onDestroy()
        transcribeKit.release()
        voiceKit.release()
    }

    override fun executeCommand(
        command: ServiceCommand,
        handler: (ServiceEvent) -> Unit
    ) {
        super.executeCommand(command, handler)
        when (command) {
            is StartListen -> {
                startListen()
            }

            is StopListen -> {
                stopListen()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startListen() {
        scope.launch {
            voiceKit.start()
        }
    }

    private fun stopListen() {
        scope.launch {
            voiceKit.stop()
        }
    }

    class STTServiceConnector(
        context: Context,
    ) : ServiceConnector<STTService>(context, STTService::class) {
        fun startListen() = send(StartListen)
        fun stopListen() = send(StopListen)
        fun onGotTextFromVoice(
            handler: (String) -> Unit
        ) = subscribe { event ->
            if (event is OnGotTextFromVoice) {
                handler(event.text)
            }
        }
    }

    @Serializable
    object StartListen : ServiceCommand()

    @Serializable
    object StopListen : ServiceCommand()

    @Serializable
    data class OnGotTextFromVoice(
        val text: String
    ) : ServiceEvent()

    companion object {
        private val TAG = STTService::class.simpleName

        @Composable
        fun rememberSttService() = LocalContext.current.let { context ->
            remember(context) {
                callbackFlow {
                    val connector = STTServiceConnector(context)
                    connector.connect()
                    send(connector)
                    awaitClose {
                        connector.disconnect()
                    }
                }
            }
        }.collectAsState(null)
    }
}
