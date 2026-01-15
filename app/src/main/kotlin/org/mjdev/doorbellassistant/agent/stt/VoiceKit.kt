package org.mjdev.doorbellassistant.agent.stt

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.sqrt

class VoiceKit(
    val context: Context,
    val voiceDetectionSensitivity: Float = 0.2f,
    val stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    val listener: VoiceKitListener,
    val voiceRecognitionThreshold: Float = 1500f,
    var sampleRate: Int = 16000,
    var channelCount: Int = 1,
) {
    private var audioRecord: AudioRecord? = null
    private var listeningJob: Job? = null
    private var recordingJob: Job? = null
    private val mutex = Mutex()
    private var isRecording = false
    private var isVoiceDetected = false
    private val silenceDurationMs = (stopListeningWhenNoVoiceAtLeast * 1000).toLong()

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend fun start(frequency: Int, channels: Int) = mutex.withLock {
        sampleRate = frequency
        channelCount = channels
        val channelConfig = if (channels == 1) {
            AudioFormat.CHANNEL_IN_MONO
        } else {
            AudioFormat.CHANNEL_IN_STEREO
        }
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, sampleRate / 2)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        audioRecord?.startRecording()
        listeningJob = CoroutineScope(Dispatchers.IO).launch {
            listenForVoice()
        }
    }

//    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
//    suspend fun startRecording() = mutex.withLock {
//        if (isRecording) return@withLock
//        isRecording = true
//        listener.onVoiceStarts()
//        recordingJob = CoroutineScope(Dispatchers.IO).launch {
//            recordAndProcessVoice()
//        }
//    }

    suspend fun stop() = mutex.withLock {
        listeningJob?.cancelAndJoin()
        recordingJob?.cancelAndJoin()
        audioRecord?.runCatching {
            stop()
            release()
        }
        audioRecord = null
        isRecording = false
    }

    private suspend fun listenForVoice() {
        val record = audioRecord ?: return
        val buffer = ShortArray(sampleRate / 10)
        var lastVoiceTime = System.currentTimeMillis()
        while (listeningJob?.isActive == true && !isRecording) {
            val read = record.read(buffer, 0, buffer.size)
            if (read > 0) {
                val energy = calculateRMS(buffer, read)
                if (energy > voiceRecognitionThreshold * voiceDetectionSensitivity) {
                    lastVoiceTime = System.currentTimeMillis()
                    isVoiceDetected = true
                    listener.onVoiceDetected()
                } else {
                    val silenceDuration = System.currentTimeMillis() - lastVoiceTime
                    if (isVoiceDetected && (silenceDuration > silenceDurationMs)) {
                        isVoiceDetected = false
                        listener.voiceEnds()
                    }
                }
            }
            delay(50)
        }
    }

//    private suspend fun recordAndProcessVoice() {
//        val record = audioRecord ?: return
//        val chunkSize = sampleRate / 2
//        val buffer = ShortArray(chunkSize)
//        var lastVoiceTime = System.currentTimeMillis()
//        while (recordingJob?.isActive == true && isRecording) {
//            val read = record.read(buffer, 0, buffer.size)
//            if (read > 0) {
//                val energy = calculateRMS(buffer, read)
//                if (energy > energyThreshold * voiceDetectionSensitivity) {
//                    lastVoiceTime = System.currentTimeMillis()
//                }
//                val byteArray = shortArrayToByteArray(buffer, read)
//                listener.onGotVoiceChunk(byteArray)
//                val silenceDuration = System.currentTimeMillis() - lastVoiceTime
//                if (silenceDuration > silenceDurationMs) {
//                    isRecording = false
//                    listener.voiceEnds()
//                    listenForVoice()
//                    break
//                }
//            }
//        }
//    }

    private fun calculateRMS(buffer: ShortArray, size: Int): Float {
        var sum = 0.0
        for (i in 0 until size) {
            val sample = buffer[i].toDouble()
            sum += sample * sample
        }
        return sqrt(sum / size).toFloat()
    }

    private fun shortArrayToByteArray(shorts: ShortArray, size: Int): ByteArray {
        val bytes = ByteArray(size * 2)
        for (i in 0 until size) {
            val value = shorts[i].toInt()
            bytes[i * 2] = (value and 0xFF).toByte()
            bytes[i * 2 + 1] = ((value shr 8) and 0xFF).toByte()
        }
        return bytes
    }
}