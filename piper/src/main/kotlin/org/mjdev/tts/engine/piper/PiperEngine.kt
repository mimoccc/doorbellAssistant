/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.tts.engine.piper

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mjdev.tts.engine.audio.AudioPlayer
import org.mjdev.tts.engine.base.TtsEngine
import org.mjdev.tts.engine.espeak.EspeakWrapper
import org.mjdev.tts.engine.helpers.MisakiToPiperIPA
import org.mjdev.tts.engine.misaki.G2P
import org.mjdev.tts.engine.misaki.Lexicon
import org.mjdev.tts.engine.utils.AssetUtils
import java.io.File
import java.nio.FloatBuffer
import java.nio.LongBuffer

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
@Suppress("CanBeParameter")
class PiperEngine(
    private val context: Context,
    private val language: String,
    private val sampleRate: Int,
    private val assetDir: String,
    private val modelFileName: String,
    private val jsonFileName: String,
    private val filesDir: File = context.filesDir,
) : TtsEngine {
    companion object {
        private const val TAG = "PiperEngine"
        private const val PAD = "_"
        private const val BOS = "^"
        private const val EOS = "$"
    }

    private val piperContext: CloseableCoroutineDispatcher = newSingleThreadContext("Piper")
    private val piperScope: CoroutineScope = CoroutineScope(piperContext)
    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var config: PiperConfig? = null
    private var espeak: EspeakWrapper? = null
    private var misakiG2P: G2P? = null
    private var misakiLexicon: Lexicon? = null
    private var initialized = false
    private val localFileDir = filesDir
        .resolve("piper")
        .resolve(language)
        .apply {
            if (exists().not()) mkdirs()
        }
    private val modelFile = File(localFileDir, modelFileName)
    private val jsonFile = File(localFileDir, jsonFileName)
    private val mutex = Mutex()
    private val texts = mutableListOf<String>()

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun initialize() {
        if (initialized) return
        piperScope.launch(Dispatchers.IO) {
            mutex.withLock {
                try {
                    context.assets.open("$assetDir/$language/$modelFileName").use { input ->
                        modelFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    context.assets.open("$assetDir/$language/$jsonFileName").use { input ->
                        jsonFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (!modelFile.exists()) {
                        throw (RuntimeException("Model files missing for at ${modelFile.absolutePath}"))
                    }
                    if (!jsonFile.exists()) {
                        throw (RuntimeException("Model files missing for at ${jsonFile.absolutePath}"))
                    }
                    Log.v(TAG, "STEP 2: Assets verified.")
                    val jsonString = jsonFile.readText()
                    Log.v(TAG, "STEP 3: JSON read (${jsonString.length} chars). Parsing Gson...")
                    config = Gson().fromJson(jsonString, PiperConfig::class.java)
                    if (config == null) {
                        throw (RuntimeException("Gson returned null config!"))
                    }
                    Log.v(TAG, "STEP 4: Config loaded. SampleRate=${config?.audio?.sampleRate}")
                    Log.v(TAG, "STEP 5: Initializing ONNX Environment...")
                    ortEnv = OrtEnvironment.getEnvironment()
                    Log.v(TAG, "ONNX Environment created. Creating SessionOptions...")
                    val opts = OrtSession.SessionOptions().apply {
                        setIntraOpNumThreads(1)
                        setExecutionMode(OrtSession.SessionOptions.ExecutionMode.PARALLEL)
                        setMemoryPatternOptimization(false)
                    }
                    Log.v(TAG, "Creating ONNX Session from ${modelFile.absolutePath}...")
                    ortSession = ortEnv?.createSession(
                        modelFile.absolutePath,
                        opts
                    )
                    Log.v(TAG, "ONNX Session created.")
                    Log.v(TAG, "STEP 6: ONNX Ready.")
                    Log.v(TAG, "STEP 7: Checking Espeak Data...")
                    val dataDir = File(context.filesDir, "espeak-ng-data")
                    if (!dataDir.exists()) {
                        Log.v(TAG, "Extracting espeak-ng-data...")
                        AssetUtils.extractAssets(
                            context,
                            "espeak-ng-data",
                            context.filesDir
                        )
                    }
                    Log.v(TAG, "Initializing EspeakWrapper JNI...")
                    espeak = EspeakWrapper()
                    val res = espeak?.initializeSafe(context.filesDir.absolutePath)
                    if (res == -1) {
                        throw (RuntimeException("Espeak init failed (JNI returned -1)"))
                    }
                    Log.v(TAG, "STEP 8: Espeak Ready.")
                    val espeakVoice = config?.espeak?.voice ?: "en-us"
                    Log.v(TAG, "STEP 9: Init Misaki (Voice=$espeakVoice)...")
                    if (espeakVoice.startsWith("en")) {
                        try {
                            val isBritish = espeakVoice.contains("gb")
                            misakiLexicon = Lexicon(context, isBritish)
                            misakiLexicon?.load()
                            misakiG2P = G2P(misakiLexicon!!) { word ->
                                val espeakPhonemes = espeak?.textToPhonemesSafe(word, espeakVoice)
                                espeakPhonemes
                            }
                            Log.i(TAG, "Misaki G2P initialized (british=$isBritish)")
                        } catch (e: Exception) {
                            Log.w(TAG, "Misaki init failed, using eSpeak only", e)
                            misakiG2P = null
                        }
                    }
                    initialized = true
                    Log.i(TAG, "PiperEngine initialized successfully.")
                    playNextText()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize PiperEngine", e)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun playNextText() = piperScope.launch {
        delay(200)
        mutex.withLock {
            if (texts.isNotEmpty()) {
                val t = texts.first()
                texts.removeFirst()
                generate(
                    autoplay = true,
                    text = t,
                    onPlayerFinish = {
//                        Handler().postDelayed() {
//                            playNextText()
//                        }
                    }
                ) {
                    // no op
                }
            }
        }
    }

    override fun generate(
        text: String,
        autoplay: Boolean,
        speed: Float,
        voice: String?,
        onPlayerFinish: () -> Unit,
        callback: (FloatArray) -> Unit,
    ) {
        if (!initialized) {
            Log.e(TAG, "Uninitialized PiperEngine.")
            texts.add(text)
        } else {
            piperScope.launch {
                mutex.withLock {
                    val session = ortSession!!
                    val env = ortEnv!!
                    val conf = config!!
                    val rawPhonemes: String
                    if (misakiG2P != null) {
                        val misakiResult = misakiG2P!!.phonemize(text)
                        rawPhonemes = MisakiToPiperIPA.convert(misakiResult)
                        Log.d(TAG, "Misaki Phonemes: $misakiResult")
                        Log.d(TAG, "Converted to IPA: $rawPhonemes")
                    } else {
                        rawPhonemes = espeak?.textToPhonemesSafe(text, conf.espeak.voice) ?: ""
                        Log.d(TAG, "eSpeak Phonemes: $rawPhonemes")
                    }
                    Log.d(TAG, "Raw Phonemes: $rawPhonemes")
                    val idMap = conf.phonemeIdMap
                    val tokenIds = mutableListOf<Long>()
                    idMap[BOS]?.forEach { tokenIds.add(it.toLong()) }
                    idMap[PAD]?.forEach { tokenIds.add(it.toLong()) }
                    for (char in rawPhonemes) {
                        val charStr = char.toString()
                        if (idMap.containsKey(charStr)) {
                            idMap[charStr]?.forEach { tokenIds.add(it.toLong()) }
                            idMap[PAD]?.forEach { tokenIds.add(it.toLong()) }
                        } else {
                            // ???
                        }
                    }
                    idMap[EOS]?.forEach { tokenIds.add(it.toLong()) }
                    if (tokenIds.size <= 2) {
                        Log.e(
                            TAG, "No valid phonemes generated for text: '${
                                text
                            }'. Voice likely does not support this language/script."
                        )
                    } else {
                        val inputIds = tokenIds.toLongArray()
                        val inputLengths = longArrayOf(inputIds.size.toLong())
                        val baseLengthScale = conf.inference.lengthScale
                        val finalLengthScale = baseLengthScale / speed
                        Log.i(
                            TAG,
                            "Generating with speed=$speed, baseScale=$baseLengthScale, finalScale=$finalLengthScale. Token Count: ${tokenIds.size}"
                        )
                        Log.d(TAG, "Token IDs: $tokenIds")
                        val scales = floatArrayOf(
                            conf.inference.noiseScale,
                            finalLengthScale,
                            conf.inference.noiseW
                        )
                        val inputTensor = OnnxTensor.createTensor(
                            env,
                            LongBuffer.wrap(inputIds),
                            longArrayOf(1, inputIds.size.toLong())
                        )
                        val lengthTensor = OnnxTensor
                            .createTensor(env, LongBuffer.wrap(inputLengths), longArrayOf(1))
                        val scalesTensor = OnnxTensor
                            .createTensor(env, FloatBuffer.wrap(scales), longArrayOf(3))
                        val inputs = mapOf(
                            "input" to inputTensor,
                            "input_lengths" to lengthTensor,
                            "scales" to scalesTensor
                        )
                        try {
                            val outputs = session.run(inputs)
                            val audioTensor =
                                outputs[0] as OnnxTensor // Output is usually just 'output'
                            val floatBuf = audioTensor.floatBuffer
                            val audio = FloatArray(floatBuf.remaining())
                            floatBuf.get(audio)
                            callback(audio)
                            if (autoplay) {
                                AudioPlayer(sampleRate).play(audio) {
                                    onPlayerFinish()
                                }
                            }
                            outputs.close()
                        } catch (e: Exception) {
                            Log.e(TAG, "Inference failed", e)
                        } finally {
                            inputTensor.close()
                            lengthTensor.close()
                            scalesTensor.close()
                        }
                    }
                }
            }
        }
    }

    override fun getSampleRate(): Int = config?.audio?.sampleRate ?: 22050
    override fun getVoices(): List<String> = listOf("en_US-amy-medium") // Hardcoded for now
    override fun isInitialized(): Boolean = initialized

    override fun release() {
        if (!initialized) return
        piperScope.launch {
            mutex.withLock {
                ortSession?.close()
                ortEnv?.close()
                initialized = false
            }
        }
    }
}
