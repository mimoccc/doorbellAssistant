package org.mjdev.tts.engine.espeak

class EspeakWrapper {
    companion object {
        init {
            try {
                System.loadLibrary("piper")
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        private val globalLock = this
        private var isNativeInitialized = false
    }

    private external fun initialize(dataPath: String): Int
    private external fun textToPhonemes(text: String, language: String): String

    fun initializeSafe(dataPath: String): Int = synchronized(globalLock) {
        if (isNativeInitialized) {
            return 0
        }
        val res = initialize(dataPath)
        if (res >= 0) {
             isNativeInitialized = true
        }
        return res
    }

    fun textToPhonemesSafe(text: String, language: String): String = synchronized(globalLock) {
        return textToPhonemes(text, language)
    }
}