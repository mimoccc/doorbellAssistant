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

import androidx.annotation.ColorInt
import androidx.annotation.ColorLong
import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color

object ColorExt {
    fun Color.lighter(
        @FloatRange(from = 0.0, to = 1.0)
        ratio: Float
    ): Color {
        val r = red
        val g = green
        val b = blue
        val a = alpha
        val k = ratio.coerceIn(0f, 1f)
        val nr = r + (1f - r) * k
        val ng = g + (1f - g) * k
        val nb = b + (1f - b) * k
        return Color(nr, ng, nb, a)
    }

    fun Color.darker(
        @FloatRange(from = 0.0, to = 1.0)
        ratio: Float
    ): Color {
        val r = red
        val g = green
        val b = blue
        val a = alpha
        val k = ratio.coerceIn(0f, 1f)
        val nr = r * (1f - k)
        val ng = g * (1f - k)
        val nb = b * (1f - k)
        return Color(nr, ng, nb, a)
    }

    @ColorLong
    fun Color.toColorLong(): Long {
        return if ((value and 0x3FUL) < 16UL) {
            value
        } else {
            (value and 0x3FUL.inv()) or ((value and 0x3FUL) - 1UL)
        }.toLong()
    }

    @ColorInt
    fun Color.toColorInt(): Int {
        return if ((value and 0x3FUL) < 16UL) {
            value
        } else {
            (value and 0x3FUL.inv()) or ((value and 0x3FUL) - 1UL)
        }.toInt()
    }
}
