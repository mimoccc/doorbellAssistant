/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.tts.helpers

object MisakiToPiperIPA {
    private val DIPHTHONG_MAP = mapOf(
        "A" to "eɪ",  // hey
        "I" to "aɪ",  // high
        "O" to "oʊ",  // go
        "W" to "aʊ",  // how
        "Y" to "ɔɪ",  // soy
        "Q" to "əʊ"   // British only, but include for safety
    )
    private val AFFRICATE_MAP = mapOf(
        "ʤ" to "dʒ",  // jump
        "ʧ" to "tʃ"   // church
    )
    private val OTHER_MAP = mapOf(
        "ᵊ" to "ə",   // Small schwa -> regular schwa (Piper may not have ᵊ)
        "T" to "ɾ",   // Kokoro's T -> flap (already in Piper as ɾ)
        "ɡ" to "ɡ"    // Ensure correct g character (U+0261)
    )

    fun convert(misakiPhonemes: String): String {
        var result = misakiPhonemes
        for ((misaki, ipa) in DIPHTHONG_MAP) {
            result = result.replace(misaki, ipa)
        }
        for ((misaki, ipa) in AFFRICATE_MAP) {
            result = result.replace(misaki, ipa)
        }
        for ((misaki, ipa) in OTHER_MAP) {
            result = result.replace(misaki, ipa)
        }
        return result
    }

//    fun needsConversion(phonemes: String): Boolean {
//        return DIPHTHONG_MAP.keys.any { phonemes.contains(it) } ||
//                AFFRICATE_MAP.keys.any { phonemes.contains(it) } ||
//                OTHER_MAP.keys.any { phonemes.contains(it) }
//    }
}