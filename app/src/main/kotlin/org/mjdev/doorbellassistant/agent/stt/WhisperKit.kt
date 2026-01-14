package org.mjdev.doorbellassistant.agent.stt

import android.content.Context

class WhisperKit(
    val context: Context
) {
    fun setModel(
        modelName: String = "whisper-tiny"
    ) {
    }

    fun setCallback(
        callback: (what: Int, WhisperKitResult) -> Unit
    ) {
    }

    fun init(frequency: Int, channels: Int, duration: Long) {
    }

    fun release() {
    }

    fun transcribe(data: ByteArray) {
    }
}