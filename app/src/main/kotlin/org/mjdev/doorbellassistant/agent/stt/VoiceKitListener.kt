package org.mjdev.doorbellassistant.agent.stt

interface VoiceKitListener {
    suspend fun onVoiceDetected()
    suspend fun onVoiceStarts()
    suspend fun onGotVoiceChunk(data: ByteArray)
    suspend fun voiceEnds()
}