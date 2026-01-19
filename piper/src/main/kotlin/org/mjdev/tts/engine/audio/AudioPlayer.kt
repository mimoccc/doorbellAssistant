/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.tts.engine.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AudioPlayer(
    private val sampleRate: Int,
) {
    private val bufferSize by lazy {
        AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
    }
    private val audioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
    }
    private val audioFormat by lazy {
        AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
    }
    private val audioTrack by lazy {
        AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM).build()
    }

    suspend fun play(
        audioData: FloatArray,
        onPlayFinish: () -> Unit = {}
    ) = withContext(Dispatchers.Main) {
        audioTrack.write(
            audioData,
            0,
            audioData.size,
            AudioTrack.WRITE_BLOCKING
        )
        audioTrack.play()
        while (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            delay(100)
        }
        audioTrack.release()
        onPlayFinish()
    }
}
