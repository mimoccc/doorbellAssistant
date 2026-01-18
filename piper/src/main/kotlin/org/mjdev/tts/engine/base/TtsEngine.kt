package org.mjdev.tts.engine.base

interface TtsEngine {
    fun initialize()
    fun generate(
        text: String,
        speed: Float = 1.0f,
        voice: String? = null,
        callback: (FloatArray) -> Unit
    )
    fun getSampleRate(): Int
    fun getVoices(): List<String>
    fun release()
    fun isInitialized() : Boolean
}