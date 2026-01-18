package org.mjdev.tts.engine.piper

import com.google.gson.annotations.SerializedName

data class PiperAudio(
    @SerializedName("sample_rate")
    val sampleRate: Int
)