package org.mjdev.doorbellassistant.agent.tts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.mjdev.tts.engine.piper.PiperEngine
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class PiperTTSEngine(
    private val context: Context,
    private val modelType: PiperModelType = PiperModelType.EN,
) {
    private val audioPlayer by lazy {
        AudioPlayer(
            sampleRate = modelType.sampleRate
        )
    }
    private val engine by lazy {
        PiperEngine(
            context = context.applicationContext,
            assetDir = modelType.assetDir,
            language = modelType.language,
            modelFileName = modelType.modelFileNameOnnx,
            jsonFileName = modelType.modelFileNameJson,
        )
    }

    fun initialize() = engine.initialize()

    fun talk(text: String) {
        engine.generate(text) { speakData ->
            audioPlayer.play(speakData)
        }
    }

    fun release() {
        engine.release()
        audioPlayer.release()
    }

    companion object {
        @Composable
        fun rememberPiperTTS(): PiperTTSEngine {
            val context = LocalContext.current
            val tts = remember {
                PiperTTSEngine(context)
            }
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
