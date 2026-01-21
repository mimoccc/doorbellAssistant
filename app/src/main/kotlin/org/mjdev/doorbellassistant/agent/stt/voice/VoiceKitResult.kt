/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.stt.voice

import org.mjdev.phone.helpers.json.Serializable

@Serializable
@Suppress("ArrayInDataClass")
open class VoiceKitResult {

    @Serializable
    object Initialized : VoiceKitResult()

    @Serializable
    object Released : VoiceKitResult()

    @Serializable
    object VoiceDetected : VoiceKitResult()

    @Serializable
    object VoiceLost : VoiceKitResult()

    @Serializable
    object StartRecording : VoiceKitResult()

    @Serializable
    data class Error(
        val error: Throwable
    ) : VoiceKitResult()

    @Serializable
    data class OnVoiceRecordChunk(
        val data: ByteArray
    ) : VoiceKitResult()

}
