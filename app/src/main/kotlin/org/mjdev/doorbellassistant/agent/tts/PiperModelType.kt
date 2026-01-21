/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.tts

import org.mjdev.phone.helpers.json.Serializable

@Serializable
@Suppress("unused")
open class PiperModelType(
    val assetDir: String,
    val language: String,
    val modelFileNameOnnx: String,
    val modelFileNameJson: String,
    val sampleRate: Int,
    val tuning: PiperVoiceTuning,
) {

    @Serializable
    object CS: PiperModelType(
        assetDir = "piper",
        language = "cs",
        modelFileNameOnnx = "cs.onnx",
        modelFileNameJson = "cs.json",
        sampleRate = 22050,
        tuning = PiperVoiceTuning.YoungCool,
    )

    @Serializable
    object EN : PiperModelType(
        assetDir = "piper",
        language = "en",
        modelFileNameOnnx = "en.onnx",
        modelFileNameJson = "en.json",
        sampleRate = 16000,
        tuning = PiperVoiceTuning.YoungCool,
    )

}
