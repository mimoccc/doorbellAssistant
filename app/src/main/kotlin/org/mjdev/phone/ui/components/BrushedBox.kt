package org.mjdev.phone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.shape.InverseOvalShape
import org.mjdev.phone.ui.theme.base.phoneColors

@Previews
@Composable
fun BrushedBox(
    modifier: Modifier = Modifier,
    outerColor: Color? = Color.Black,
    innerColor: Color = Color.White,
    paddingLeft: Dp = 0.dp,
    paddingTop: Dp = 0.dp,
    paddingRight: Dp = 0.dp,
    paddingBottom: Dp = 0.dp,
) {
    val background = outerColor ?: phoneColors.background
    val shape = InverseOvalShape(
        paddingLeft = paddingLeft,
        paddingTop = paddingTop,
        paddingRight = paddingRight,
        paddingBottom = paddingBottom,
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
    ) {
        Box(Modifier.fillMaxSize().background(innerColor))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.99f
                }
                .clip(shape)
                .background(background)
        )
    }
}
