/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.mjdev.phone.helpers.Previews
import kotlin.math.sqrt

@Previews
@Composable
fun OvalShadow(
    modifier: Modifier = Modifier,
    colorStart: Color = Color.White,
    colorEnd: Color = Color.Black,
    ratio: Float = 0.5f,
    inverse: Boolean = false,
) {
    val color1 = if (inverse) colorEnd else colorStart
    val color2 = if (inverse) colorStart else colorEnd
    Spacer(
        modifier = modifier.drawBehind {
            val centerPos = size.center
            val maxSide = size.maxDimension
            val brush = Brush.radialGradient(
                1f - 2f * ratio to color1,
                2f - 2f * ratio to color2,
                center = centerPos,
                radius = maxSide / sqrt(2f)
            )
            drawRect(brush)
        }
    )
}