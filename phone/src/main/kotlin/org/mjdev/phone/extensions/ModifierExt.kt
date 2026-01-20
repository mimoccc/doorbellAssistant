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

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mjdev.phone.extensions.CustomExt.drawNeonStroke

object ModifierExt {
    fun Modifier.applyIf(
        condition: Boolean,
        other: Modifier.() -> Modifier
    ): Modifier = if (condition) this.then(other()) else this

    fun Modifier.dashedBorder(
        brush: Brush,
        shape: Shape,
        strokeWidth: Dp = 2.dp,
        dashLength: Dp = 4.dp,
        gapLength: Dp = 4.dp,
        cap: StrokeCap = StrokeCap.Round
    ) = drawWithContent {
        val outline = shape.createOutline(size, layoutDirection, density = this)
        val dashedStroke = Stroke(
            cap = cap,
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx())
            )
        )
        drawContent()
        drawOutline(
            outline = outline,
            style = dashedStroke,
            brush = brush
        )
    }

    fun Modifier.dashedBorder(
        color: Color = Color.White,
        shape: Shape = RectangleShape,
        strokeWidth: Dp = 2.dp,
        dashLength: Dp = 4.dp,
        gapLength: Dp = 4.dp,
        cap: StrokeCap = StrokeCap.Round
    ) = dashedBorder(brush = SolidColor(color), shape, strokeWidth, dashLength, gapLength, cap)

    fun Modifier.neonStroke(
        backgroundColor: Color = Color.Transparent,
        glowColor: Color = Color.White,
        glowRadius: Float = 8f,
        shape: Shape = CircleShape,
    ) = drawWithContent {
        drawNeonStroke(
            color = glowColor,
            backgroundColor = backgroundColor,
            radius = glowRadius.dp,
            shape = shape
        )
        drawContent()
    }
}
