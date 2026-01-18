package org.mjdev.doorbellassistant.agent.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

@Suppress("CanBeParameter")
class AudioPlayer(
    private val sampleRate: Int
) {
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_FLOAT
    )

    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    fun play(
        audioData: FloatArray
    ) {
        audioTrack.pause()
        audioTrack.flush()
        audioTrack.play()
        audioTrack.write(
            audioData,
            0,
            audioData.size,
            AudioTrack.WRITE_BLOCKING
        )
    }
    
    fun release() = audioTrack.release()
}
