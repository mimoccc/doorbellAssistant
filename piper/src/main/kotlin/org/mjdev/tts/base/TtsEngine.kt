/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.tts.base

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