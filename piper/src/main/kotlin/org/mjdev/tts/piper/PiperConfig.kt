/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.tts.piper

import com.google.gson.annotations.SerializedName

data class PiperConfig(
    val audio: PiperAudio,
    val espeak: PiperEspeak,
    val inference: PiperInference,
    @SerializedName("phoneme_id_map")
    val phonemeIdMap: Map<String, List<Int>>,
    @SerializedName("speaker_id_map")
    val speakerIdMap: Map<String, Int> = emptyMap()
)