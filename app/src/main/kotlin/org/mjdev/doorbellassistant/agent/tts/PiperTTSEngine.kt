/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.tts

import android.content.Context
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.tts.engine.piper.PiperEngine

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class PiperTTSEngine(
    private val context: Context,
    private val modelType: PiperModelType = PiperModelType.EN,
) {
    private val engine by lazy {
        PiperEngine(
            context = context.applicationContext,
            assetDir = modelType.assetDir,
            language = modelType.language,
            modelFileName = modelType.modelFileNameOnnx,
            jsonFileName = modelType.modelFileNameJson,
            sampleRate = modelType.sampleRate
        )
    }

    fun initialize() =
        engine.initialize()

    fun talk(text: String) {
        engine.generate(text, autoplay = true) {
            // no op
        }
    }

    fun release() {
        engine.release()
    }
}
