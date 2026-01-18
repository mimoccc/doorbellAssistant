package org.mjdev.tts.engine.data

data class MToken(
    val text: String,
    val tag: String,
    var whitespace: String,
    var phonemes: String? = null,
    val startTs: Float? = null,
    val endTs: Float? = null,
    val attributes: Underscore = Underscore()
)