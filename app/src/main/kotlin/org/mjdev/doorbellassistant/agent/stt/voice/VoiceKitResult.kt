package org.mjdev.doorbellassistant.agent.stt.voice

@Suppress("ArrayInDataClass")
sealed class VoiceKitResult {
    object Initialized : VoiceKitResult()

    object Released : VoiceKitResult()

    object VoiceDetected : VoiceKitResult()

    object VoiceLost : VoiceKitResult()

    object StartRecording : VoiceKitResult()

    data class Error(
        val error: Throwable
    ) : VoiceKitResult()

    data class OnVoiceRecordChunk(
        val data: ByteArray
    ) : VoiceKitResult()
}
