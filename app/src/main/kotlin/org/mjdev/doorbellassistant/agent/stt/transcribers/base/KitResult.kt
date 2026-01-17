package org.mjdev.doorbellassistant.agent.stt.transcribers.base

sealed class KitResult {
    object Initialized : KitResult()

    object Released : KitResult()

    object Transcribing : KitResult()

    data class Error(val error: Throwable) : KitResult()

    data class Download(val percent: Float) : KitResult()

    data class Text(
        val text: String = "",
        val segments: List<String> = listOf(),
    ) : KitResult()
}