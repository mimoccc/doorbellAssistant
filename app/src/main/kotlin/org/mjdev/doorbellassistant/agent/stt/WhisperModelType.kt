package org.mjdev.doorbellassistant.agent.stt

enum class WhisperModelType(
    val modelName: String,
    val url: String,
    val frequency: Int,
    val channels: Int,
    val duration: Long,
) {
    SMALL(
        modelName = "whisper-small-multilingual",
        url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin",
        frequency = 16000,
        channels = 1,
        duration = 0
    ),
    MEDIUM(
        modelName = "whisper-medium-multilingual",
        url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin",
        frequency = 16000,
        channels = 1,
        duration = 0
    ),
    LARGE(
        modelName = "whisper-large-v3",
        url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.bin",
        frequency = 16000,
        channels = 1,
        duration = 0
    )
}