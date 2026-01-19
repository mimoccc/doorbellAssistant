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
    fun String.toInetAddress(): InetAddress? = when {
        isValidIpAddress() -> runCatching {
            InetAddress.getByName(this)
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()

        else -> {
            Exception("Invalid ip address: $this").printStackTrace()
            null
        }
    }

    fun String.isValidIpAddress(): Boolean = split(".").let { parts ->
        parts.size == 4 && parts.all { it.toIntOrNull() in 0..255 }
    }
}
