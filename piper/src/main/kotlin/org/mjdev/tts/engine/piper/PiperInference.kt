package org.mjdev.tts.engine.piper

import com.google.gson.annotations.SerializedName

data class PiperInference(
    @SerializedName("noise_scale")
    val noiseScale: Float = 0.667f,
    @SerializedName("length_scale")
    val lengthScale: Float = 1.0f,
    @SerializedName("noise_w")
    val noiseW: Float = 0.8f
)