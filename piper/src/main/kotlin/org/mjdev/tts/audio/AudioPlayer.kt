/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.tts.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToLong

class AudioPlayer(
    private val sampleRate: Int,
    private val outputStream: AudioOutputStream = AudioOutputStream.MEDIA
) {
    private fun getAudioAttributes() = AudioAttributes.Builder()
        .setUsage(outputStream.toUsage())
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

    suspend fun playOggFromAssets(
        context: Context,
        assetPath: String,
        onPlayFinish: suspend () -> Unit = {},
        onPlayError: suspend (Throwable) -> Unit = {}
    ) = runCatching {
        val tempFile = File(context.cacheDir, "temp_ringtone.ogg")
        context.assets.open(assetPath).use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        playOgg(tempFile, onPlayFinish, onPlayError)
        tempFile.delete()
    }.onFailure { e ->
        onPlayError(e)
    }

    suspend fun playOgg(
        file: File,
        onPlayFinish: suspend () -> Unit = {},
        onPlayError: suspend (Throwable) -> Unit = {}
    ) = runCatching {
        suspendCancellableCoroutine { continuation ->
            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(getAudioAttributes())
                setOnCompletionListener {
                    it.release()
                    continuation.resume(Unit)
                }
                setOnErrorListener { mp, what, extra ->
                    mp.release()
                    continuation.resumeWithException(
                        Exception("MediaPlayer error: what=$what, extra=$extra")
                    )
                    true
                }
                continuation.invokeOnCancellation {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
            }
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }
        onPlayFinish()
    }.onFailure { e ->
        onPlayError(e)
    }

    suspend fun playOgg(
        context: Context,
        resourceId: Int,
        onPlayFinish: suspend () -> Unit = {},
        onPlayError: suspend (Throwable) -> Unit = {}
    ) = runCatching {
        suspendCancellableCoroutine { continuation ->
            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(getAudioAttributes())
                setOnCompletionListener {
                    it.release()
                    continuation.resume(Unit)
                }
                setOnErrorListener { mp, what, extra ->
                    mp.release()
                    continuation.resumeWithException(
                        Exception("MediaPlayer error: what=$what, extra=$extra")
                    )
                    true
                }
                continuation.invokeOnCancellation {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
            }
            val afd = context.resources.openRawResourceFd(resourceId)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mediaPlayer.prepare()
            mediaPlayer.start()
        }
        onPlayFinish()
    }.onFailure { e ->
        onPlayError(e)
    }

    enum class AudioOutputStream {
        SONIFICATION,
        MEDIA,
        ALARM,
        NOTIFICATION,
        VOICE_COMMUNICATION,
        ASSISTANT;

        fun toUsage(): Int = when (this) {
            SONIFICATION -> AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
            MEDIA -> AudioAttributes.USAGE_MEDIA
            ALARM -> AudioAttributes.USAGE_ALARM
            NOTIFICATION -> AudioAttributes.USAGE_NOTIFICATION
            VOICE_COMMUNICATION -> AudioAttributes.USAGE_VOICE_COMMUNICATION
            ASSISTANT -> AudioAttributes.USAGE_ASSISTANT
        }
    }
}
