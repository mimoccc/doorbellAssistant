package org.mjdev.phone.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mjdev.phone.helpers.Previews

// todo
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
//    val background = outerColor ?: phoneColors.background
//    val shape = InverseOvalShape(
//        paddingLeft = paddingLeft,
//        paddingTop = paddingTop,
//        paddingRight = paddingRight,
//        paddingBottom = paddingBottom,
//    )
//    Box(
//        modifier = modifier
//            .graphicsLayer {
//                compositingStrategy = CompositingStrategy.Offscreen
//            }
//    ) {
//        Box(Modifier.fillMaxSize().background(innerColor))
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .graphicsLayer {
//                    alpha = 0.99f
//                }
//                .clip(shape)
//                .background(background)
//        )
//    }
}
