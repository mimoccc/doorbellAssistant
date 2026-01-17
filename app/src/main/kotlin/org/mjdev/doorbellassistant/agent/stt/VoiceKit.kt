package org.mjdev.doorbellassistant.agent.stt

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import io.ktor.server.engine.launchOnCancellation
import io.ktor.utils.io.InternalAPI
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
    val maxRecordingDurationMs: Long = 20000L,
    val minRecordingDurationMs: Long = 2000L,
) {
    private var audioRecord: AudioRecord? = null
    private var listeningJob: Job? = null
    private var recordingJob: Job? = null
    private val mutex = Mutex()
    private var isRecording = false
    private var isVoiceDetected = false
    private var isListeningActive = false
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
        isListeningActive = true
        listeningJob = CoroutineScope(Dispatchers.IO).launch {
            listenForVoiceContinuously()
        }
    }

    @OptIn(InternalAPI::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private suspend fun startRecording() = mutex.withLock {
        if (isRecording) return@withLock
        isRecording = true
        isVoiceDetected = true
        listener.onVoiceStarts()
        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            recordAndProcessVoice()
        }
    }

    suspend fun stop() = mutex.withLock {
        isListeningActive = false
        listeningJob?.cancelAndJoin()
        recordingJob?.cancelAndJoin()
        audioRecord?.runCatching {
            stop()
            release()
        }
        audioRecord = null
        isRecording = false
        isVoiceDetected = false
    }

    @SuppressLint("MissingPermission")
    private suspend fun listenForVoiceContinuously() {
        val record = audioRecord ?: return
        val buffer = ShortArray(sampleRate / 10)
        while (listeningJob?.isActive == true && isListeningActive) {
            if (isRecording) {
                delay(100)
                continue
            }
            val read = record.read(buffer, 0, buffer.size)
            if (read > 0) {
                val energy = calculateRMS(buffer, read)
                if (energy > voiceRecognitionThreshold * voiceDetectionSensitivity) {
                    listener.onVoiceDetected()
                    startRecording()
                }
            }
            delay(50)
        }
    }

    private suspend fun recordAndProcessVoice() {
        val record = audioRecord ?: return
        val chunkSize = sampleRate / 2
        val buffer = ShortArray(chunkSize)
        val startTime = System.currentTimeMillis()
        var lastVoiceTime = System.currentTimeMillis()
        var hasVoiceBeenDetected = false
        try {
            while (recordingJob?.isActive == true && isRecording) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    val energy = calculateRMS(buffer, read)
                    val currentTime = System.currentTimeMillis()
                    if (energy > voiceRecognitionThreshold * voiceDetectionSensitivity) {
                        lastVoiceTime = currentTime
                        hasVoiceBeenDetected = true
                        if (!isVoiceDetected) {
                            isVoiceDetected = true
                            listener.onVoiceDetected()
                        }
                    }
                    val byteArray = shortArrayToByteArray(buffer, read)
                    listener.onGotVoiceChunk(byteArray)
                    val totalRecordingTime = currentTime - startTime
                    val silenceDuration = currentTime - lastVoiceTime
                    if (totalRecordingTime >= maxRecordingDurationMs ||
                        (totalRecordingTime >= minRecordingDurationMs && 
                         hasVoiceBeenDetected && 
                         silenceDuration > silenceDurationMs)) {
                        break
                    }
                }
                delay(10)
            }
        } finally {
            isRecording = false
            isVoiceDetected = false
            listener.voiceEnds()
        }
    }

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