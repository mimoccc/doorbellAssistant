/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: [mimoccc@gmail.com](mailto:mimoccc@gmail.com)
 * e: [mj@mjdev.org](mailto:mj@mjdev.org)
 * w: [https://mjdev.org](https://mjdev.org)
 * w: [https://github.com/mimoccc](https://github.com/mimoccc)
 * w: [https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/](https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/)
 */

package org.mjdev.doorbellassistant.agent.tts

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.mjdev.tts.engine.audio.AudioPlayer
import org.mjdev.tts.engine.piper.PiperEngine
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.resume
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class PiperTTSEngine(
    private val context: Context,
    private val modelType: PiperModelType = PiperModelType.CS,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val player by lazy { AudioPlayer(modelType.sampleRate) }
    private val fileCache by lazy { FileCache(context, PIPER_CACHE) }
    private val engine by lazy {
        PiperEngine(
            context = context.applicationContext,
            assetDir = modelType.assetDir,
            language = modelType.language,
            sampleRate = modelType.sampleRate,
            modelFileName = modelType.modelFileNameOnnx,
            jsonFileName = modelType.modelFileNameJson,
            lengthScale = modelType.tuning.lengthScale
        )
    }
    private val engineInit = scope.async(start = CoroutineStart.LAZY) {
        if (!engine.isInitialized()) {
            engine.initialize()
            var attempts = 0
            while (!engine.isInitialized() && attempts < PIPER_MAX_RETRY_TALK) {
                delay(100)
                attempts++
            }
        }
    }
    private var playJob: Job? = null

    fun initialize() {
        engineInit.start()
    }

    fun talk(text: String) = scope.launch {
        engineInit.await()
        while (!engine.isInitialized()) delay(300)
        val cached = fileCache[text]
        if (cached != null) {
            cached.readVoiceData()?.let { voiceData ->
                play(voiceData)
            }
        } else {
            generateVoice(text)?.let { voiceData ->
                fileCache.save(text, modelType.language, voiceData)
                play(voiceData)
            }
        }
    }

    private suspend fun generateVoice(
        text: String
    ): FloatArray? = suspendCancellableCoroutine { cont ->
        try {
            engine.generate(text) { data ->
                if (cont.isActive) cont.resume(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (cont.isActive) cont.resume(null)
        }
    }

    private fun play(voiceData: FloatArray) {
        playJob?.cancel()
        playJob = scope.launch {
            player.play(
                voiceData,
                onPlayFinish = {
                    playJob?.cancel()
                    playJob = null
                },
                onPlayError = {
                    playJob?.cancel()
                    playJob = null
                    play(voiceData)
                }
            )
        }
    }

    fun release() = engine.release()

    @Suppress("unused")
    class CachedVoice(
        val uuid: String,
        val language: String,
        val text: String,
        val file: File
    ) {
        fun readVoiceData(): FloatArray? = runCatching {
            val allBytes = file.readBytes()
            val separatorIndex = allBytes.indexOf('\n'.code.toByte())
            if (separatorIndex != -1) {
                allBytes.copyOfRange(separatorIndex + 1, allBytes.size).toFloatArray()
            } else null
        }.getOrNull()
    }

    class FileCache(
        context: Context,
        dirName: String,
        private val dir: File = File(context.filesDir, dirName)
    ) : LinkedHashMap<String, CachedVoice>() {

        init {
            if (!dir.exists()) dir.mkdirs()
            loadCache()
        }

        private fun loadCache() = dir.listFiles()
            ?.filter { it.extension == PIPER_CACHED_FILE }
            ?.forEach { file ->
                val voice = createVoiceMetadata(file)
                put(voice.text, voice)
            }

        private fun createVoiceMetadata(file: File): CachedVoice {
            val parts = file.nameWithoutExtension.split(".")
            return CachedVoice(
                uuid = parts.getOrNull(0) ?: "",
                language = parts.getOrNull(1) ?: "",
                text = file.bufferedReader().use { it.readLine() ?: "" },
                file = file
            )
        }

        @OptIn(ExperimentalUuidApi::class)
        fun save(text: String, language: String, voice: FloatArray) {
            val uuid = Uuid.random().toString()
            val file = File(dir, "$uuid.$language.$PIPER_CACHED_FILE")
            file.outputStream().use { os ->
                os.write("$text\n".toByteArray())
                os.write(voice.toByteArray())
                os.flush()
            }
            put(text, CachedVoice(uuid, language, text, file))
        }
    }

    companion object {
        private const val PIPER_CACHE = "piper-cache"
        private const val PIPER_CACHED_FILE = "pcf"
        private const val MAX_PCM_S16 = 32767f
        private const val PIPER_MAX_RETRY_TALK = 50

        fun FloatArray.toByteArray(): ByteArray = ByteBuffer
            .allocate(size * 2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply {
                forEach { f ->
                    putShort((f.coerceIn(-1f, 1f) * MAX_PCM_S16).toInt().toShort())
                }
            }
            .array()

        fun ByteArray.toFloatArray(): FloatArray = FloatArray(size / 2).apply {
            val buffer = ByteBuffer.wrap(this@toFloatArray).order(ByteOrder.LITTLE_ENDIAN)
            indices.forEach { i ->
                if (buffer.remaining() >= 2) this[i] = buffer.short / MAX_PCM_S16
            }
        }
    }
}
