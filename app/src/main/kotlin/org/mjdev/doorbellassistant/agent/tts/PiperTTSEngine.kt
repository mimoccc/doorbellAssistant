package org.mjdev.doorbellassistant.agent.tts

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.content.res.AssetManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Suppress("CanBeParameter")
class PiperTTSEngine(
    private val context: Context,
    private val sampleRate: Int = 22050,
    private val model: PiperModelType = PiperModelType.CS,
) {
    private val env = OrtEnvironment.getEnvironment()
    private val player = AudioPlayer(sampleRate)
    private val assetManager = context.assets
    private var session: OrtSession? = null
    private var configJson: String? = null

    private fun File.ensureFromAssets(
        assets: AssetManager,
        fileName: String
    ) = takeIf { it.exists() } ?: assets.open(fileName).use { input ->
        outputStream().use(input::copyTo)
    }

    private fun AssetManager.readText(
        fileName: String
    ) = open(fileName)
        .bufferedReader()
        .use { it.readText() }

    fun initialize() = runCatching {
        val modelFile = File(
            context.cacheDir,
            model.modelFileNameOnnx
        ).apply {
            ensureFromAssets(
                assets = assetManager,
                fileName = model.modelFileNameOnnx
            )
        }
        configJson = assetManager.readText(
            fileName = model.modelFileNameJson
        )
        session = env.createSession(
            modelFile.absolutePath,
            OrtSession.SessionOptions()
        )
    }.onFailure { e ->
        e.printStackTrace()
    }

    fun talk(
        text: String,
        playImmediately: Boolean = true,
        onAudioGenerated: (audioData: FloatArray) -> Unit = {}
    ) {
        try {
            val tokens = tokenizeText(text)
            val inputName = "input_ids"
            val inputTensor = OnnxTensor.createTensor(
                env,
                arrayOf(tokens)
            )
            val result = session?.run(mapOf(inputName to inputTensor))
            if (result != null) {
                val audioTensor: OnnxTensor? = result.get(0) as? OnnxTensor?
                if (audioTensor != null) {
                    val audioData = audioTensor.floatBuffer?.array()
                    if (audioData != null) {
                        onAudioGenerated(audioData)
                        if (playImmediately) {
                            player.play(audioData)
                        }
                    } else {
                        throw (RuntimeException("No audio generated. AudioData is null."))
                    }
                    audioTensor.close()
                } else {
                    throw (RuntimeException("No audio tensor. AudioTensor is null."))
                }
            } else {
                throw (RuntimeException("No result for session. Session is null.."))
            }
            inputTensor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun tokenizeText(
        text: String
    ): LongArray = text.map {
        it.code.toLong()
    }.toLongArray()

    fun release() {
        session?.close()
        env.close()
        player.release()
    }

    companion object {
        @Composable
        fun rememberPiperTTS(): PiperTTSEngine {
            val context = LocalContext.current
            val tts = remember { PiperTTSEngine(context) }
            DisposableEffect(context, tts) {
                tts.initialize()
                onDispose {
                    tts.release()
                }
            }
            return tts
        }
    }
}
