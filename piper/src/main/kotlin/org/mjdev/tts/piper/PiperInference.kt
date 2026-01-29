/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.tts.piper

import com.google.gson.annotations.SerializedName

data class PiperInference(
    @SerializedName("noise_scale")
    val noiseScale: Float = 0.667f,
    @SerializedName("length_scale")
    val lengthScale: Float = 1.0f,
    @SerializedName("noise_w")
    val noiseW: Float = 0.8f
)