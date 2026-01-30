/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

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
    val fileName
        get() = "$modelName.bin"

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
