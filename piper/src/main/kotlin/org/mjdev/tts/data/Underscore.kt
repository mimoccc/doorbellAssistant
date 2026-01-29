/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.tts.data

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