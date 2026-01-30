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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
    private val scope = CoroutineScope(Dispatchers.IO)
    private val transcribeKit by lazy {
        createKit(baseContext).apply {
            setModel(modelType)
            subscribe { event ->
                Log.d(TAG, "$event")
                when (event) {
                    is ITKitResult.Initialized -> {
                        // todo event
                    }

                    is ITKitResult.Text -> {
                        if (event.text.isNotEmpty()) {
                            sendServiceEvent(OnGotTextFromVoice(event.text))
                        }
                    }

                    is ITKitResult.Error -> {
                        sendServiceEvent(OnError(event.error))
                    }

                    is ITKitResult.Transcribing -> {
                        sendServiceEvent(OnTranscribing)
                    }

                    is ITKitResult.Download -> {
                        sendServiceEvent(OnDownloading(event.percent))
                    }

                    is ITKitResult.Released -> {
                        sendServiceEvent(OnStopListen)
                    }
                }
            }
        }
    }
    private val voiceKit by lazy {
        VoiceKit(
            context = applicationContext,
            voiceDetectionSensitivity = voiceDetectionSensitivity,
            stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
            maxRecordingDurationMs = maxRecordingDurationMs,
            minRecordingDurationMs = minRecordingDurationMs,
        ) {
            subscribe { event ->
                Log.d(TAG, "VKEvent : $event")
                when (event) {
                    is VoiceKitResult.Error -> {
                        sendServiceEvent(OnError(event.error))
                    }

                    is VoiceKitResult.Initialized -> {
                        sendServiceEvent(OnStartListen)
                    }

                    is VoiceKitResult.Released -> {
                        sendServiceEvent(OnStopListen)
                    }

                    is VoiceKitResult.StartRecording -> {
                        sendServiceEvent(OnStartListen)
                    }

                    is VoiceKitResult.VoiceDetected -> {
                        sendServiceEvent(OnVoiceDetected)
                    }

                    is VoiceKitResult.VoiceLost -> {
                        sendServiceEvent(OnVoiceLost)
                    }

                    is VoiceKitResult.OnVoiceRecordChunk -> {
                        transcribeKit.transcribe(event.data)
                        sendServiceEvent(OnVoiceContinue)
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
        when (command) {
            is StartListen -> {
                startListen()
                handler(OnStartListen)
            }

            is StopListen -> {
                stopListen()
                handler(OnStopListen)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startListen() {
        scope.launch {
            if(!voiceKit.isListeningActive) {
                voiceKit.start()
            }
        }
    }

    private fun stopListen() {
        scope.launch {
            if(voiceKit.isListeningActive) {
                voiceKit.stop()
            }
            clearEvents()
        }
    }

    class STTServiceConnector(
        context: Context,
    ) : ServiceConnector<STTService>(context, STTService::class) {
        fun startListen() = send(StartListen)
        fun stopListen() = send(StopListen)
    }

    @Serializable
    object StartListen : ServiceCommand()

    @Serializable
    object StopListen : ServiceCommand()

    @Serializable
    data class OnGotTextFromVoice(
        val text: String
    ) : ServiceEvent()

    @Serializable
    data class OnDownloading(
        val percent: Float
    ) : ServiceEvent()

    @Serializable
    data class OnError(
        val error: Throwable
    ) : ServiceEvent()

    @Serializable
    object OnTranscribing : ServiceEvent()

    @Serializable
    object OnVoiceDetected : ServiceEvent()

    @Serializable
    object OnVoiceLost : ServiceEvent()

    @Serializable
    object OnVoiceContinue : ServiceEvent()

    @Serializable
    object OnStartListen : ServiceEvent()

    @Serializable
    object OnStopListen : ServiceEvent()

    companion object {
        private val TAG = STTService::class.simpleName

        @Composable
        fun rememberSttService(
            autoStart: Boolean = false,
        ): State<STTServiceConnector?> = LocalContext.current.let { context ->
            remember(context) {
                callbackFlow {
                    val connector = STTServiceConnector(context)
                    connector.connect()
                    send(connector)
                    if (autoStart) {
                        connector.startListen()
                    }
                    awaitClose {
                        connector.disconnect()
                    }
                }
            }
        }.collectAsState(null)
    }
}
