package org.mjdev.doorbellassistant.agent.stt.transcribers.base

sealed class ITKitResult {
    object Initialized : ITKitResult()

    object Released : ITKitResult()

    object Transcribing : ITKitResult()

    data class Error(val error: Throwable) : ITKitResult()

    data class Download(val percent: Float) : ITKitResult()

    data class Text(
        val text: String = "",
        val segments: List<String> = listOf(),
    ) : ITKitResult()
}
