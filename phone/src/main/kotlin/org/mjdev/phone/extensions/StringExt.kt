/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.extensions

import java.net.InetAddress

object StringExt {

    fun String.toInetAddress(): InetAddress = split(".").map { p ->
        p.toInt().toByte()
    }.toByteArray().let { ba ->
        InetAddress.getByAddress(ba)
    }

    fun ByteArray.toInetAddress() =
        InetAddress.getByAddress(this)
}
