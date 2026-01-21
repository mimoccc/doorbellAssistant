/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.stt.transcribers.base

import org.mjdev.phone.helpers.json.Serializable

@Serializable
open class ITKitResult {

    @Serializable
    object Initialized : ITKitResult()

    @Serializable
    object Released : ITKitResult()

    @Serializable
    object Transcribing : ITKitResult()

    @Serializable
    data class Error(val error: Throwable) : ITKitResult()

    @Serializable
    data class Download(val percent: Float) : ITKitResult()

    @Serializable
    data class Text(
        val text: String = "",
        val segments: List<String> = listOf(),
    ) : ITKitResult()

}
