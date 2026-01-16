package org.mjdev.doorbellassistant.agent.stt.transcribers.whisper

import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel

@Suppress("unused")
sealed class WhisperModelType(
    override val modelName: String,
    override val url: String,
    override val frequency: Int = 16000,
    override val channels: Int = 1,
    override val assetsFolder: String = "whisper"
) : ITKitModel {
    object SMALL : WhisperModelType(
        modelName = "ggml-small",
        url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin",
    )

    object MEDIUM : WhisperModelType(
        modelName = "ggml-medium",
        url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin",
    )

    object LARGE : WhisperModelType(
        modelName = "ggml-large-v3",
        url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.bin",
    )
}
