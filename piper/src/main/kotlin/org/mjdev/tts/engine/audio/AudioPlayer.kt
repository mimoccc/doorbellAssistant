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
import kotlinx.coroutines.delay
import kotlin.math.roundToLong

// todo output to media stream
class AudioPlayer(
    private val sampleRate: Int,
) {
    private fun getAudioAttributes() = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private fun getAudioFormat() = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
        .setSampleRate(sampleRate)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    private fun createTrack(
        bufferSize: Int
    ) = AudioTrack.Builder()
        .setAudioAttributes(getAudioAttributes())
        .setAudioFormat(getAudioFormat())
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    suspend fun play(
        audioData: FloatArray,
        onPlayFinish: suspend () -> Unit = {},
        onPlayError: suspend (Throwable) -> Unit = {}
    ) = runCatching {
        val sizeInBytes = audioData.size * 4
        val track = createTrack(sizeInBytes)
        track.play()
        track.write(
            audioData,
            0,
            audioData.size,
            AudioTrack.WRITE_BLOCKING
        )
        val durationMs = (audioData.size.toFloat() / sampleRate * 1000).roundToLong()
        delay(durationMs + 200)
        track.stop()
        track.release()
        onPlayFinish()
    }.onFailure { e ->
        onPlayError(e)
    }
}
