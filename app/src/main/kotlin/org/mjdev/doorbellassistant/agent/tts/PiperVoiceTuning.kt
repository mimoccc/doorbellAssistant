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
open class PiperVoiceTuning(
    val lengthScale: Float = 0.85f,  // speed
    val noiseScale: Float = 0.5f,    // coldness
    val noiseW: Float = 0.6f ,       // energy
) {

    @Serializable
    object YoungCool : PiperVoiceTuning(lengthScale = 0.85f, noiseScale = 0.5f, noiseW = 0.6f)

    @Serializable
    object Default : PiperVoiceTuning(1.0f, 0.667f, 0.8f)

}
