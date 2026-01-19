/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.ui.shape

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Suppress("unused")
class ImageShape(
    val painter: Painter? = null,
    val orientation: Int
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = with(density) {
        Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(
                Rect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height
                )
            )
            addOval(
                Rect(
                    left = if (orientation == ORIENTATION_LANDSCAPE) (size.width - size.height)/2 else 0f,
                    top = if (orientation == ORIENTATION_LANDSCAPE) 0f else (size.height - size.width)/2,
                    right = if (orientation == ORIENTATION_LANDSCAPE) size.height else size.width,
                    bottom = if (orientation == ORIENTATION_LANDSCAPE) size.height else size.height,
                )
            )
        }.let { path ->
            Outline.Generic(path = path)
        }
    }
}
