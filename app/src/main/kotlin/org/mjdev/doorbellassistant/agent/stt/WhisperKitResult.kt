package org.mjdev.doorbellassistant.agent.stt

data class WhisperKitResult(
    val text: String = "",
    val segments: List<String> = listOf(),
)