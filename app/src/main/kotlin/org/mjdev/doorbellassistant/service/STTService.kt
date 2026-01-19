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

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitResult
import org.mjdev.doorbellassistant.agent.stt.transcribers.vosk.VoskKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.vosk.VoskModelType
import org.mjdev.doorbellassistant.agent.stt.voice.VoiceKit
import org.mjdev.doorbellassistant.agent.stt.voice.VoiceKitResult
import org.mjdev.phone.helpers.DataBus.Companion.subscribe
import org.mjdev.phone.service.BindableService

@OptIn(ExperimentalCoroutinesApi::class)
class STTService : BindableService() {
    var modelType: ITKitModel = VoskModelType.CS_SMALL
    var voiceDetectionSensitivity: Float = 0.2f
    var stopListeningWhenNoVoiceAtLeast: Float = 2.0f
    var maxRecordingDurationMs: Long = 20000L
    var minRecordingDurationMs: Long = 2000L

    private val transcribeKit by lazy {
        VoskKit(applicationContext).apply {
            setModel(modelType)
            subscribe { event ->
                Log.d(TAG, "$event")
                when (event) {
                    is ITKitResult.Initialized -> {
                    }

                    is ITKitResult.Text -> {
                    }

                    is ITKitResult.Error -> {
                    }

                    is ITKitResult.Transcribing -> {
                    }

                    is ITKitResult.Download -> {
                    }

                    is ITKitResult.Released -> {
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
                Log.d(TAG, "$event")
                when (event) {
                    is VoiceKitResult.Error -> {
                    }

                    is VoiceKitResult.Initialized -> {
                    }

                    is VoiceKitResult.Released -> {
                    }

                    is VoiceKitResult.StartRecording -> {
                    }

                    is VoiceKitResult.VoiceDetected -> {
                    }

                    is VoiceKitResult.VoiceLost -> {
                    }

                    is VoiceKitResult.OnVoiceRecordChunk -> {
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

    companion object {
        private val TAG = STTService::class.simpleName
    }
}
