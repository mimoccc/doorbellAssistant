package org.mjdev.tts.engine.piper

import com.google.gson.annotations.SerializedName
import org.mjdev.tts.engine.piper.PiperAudio
import org.mjdev.tts.engine.piper.PiperEspeak
import org.mjdev.tts.engine.piper.PiperInference

data class PiperConfig(
    val audio: PiperAudio,
    val espeak: PiperEspeak,
    val inference: PiperInference,
    @SerializedName("phoneme_id_map")
    val phonemeIdMap: Map<String, List<Int>>,
    @SerializedName("speaker_id_map")
    val speakerIdMap: Map<String, Int> = emptyMap()
)