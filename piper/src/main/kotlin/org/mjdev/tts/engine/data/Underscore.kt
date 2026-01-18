package org.mjdev.tts.engine.data

data class Underscore(
    var isHead: Boolean = false,
    var numFlags: String = "",
    var prespace: Boolean = false,
    var stress: Double? = null,
    var currency: String? = null,
    var alias: String? = null,
    var rating: Int? = null,
    var pron: String? = null,
    var acc: Int? = null,
    var moraSize: Int? = null,
    var chainFlag: Boolean = false,
    var moras: List<String>? = null,
    var accents: List<Int>? = null,
    var pitch: String? = null
)