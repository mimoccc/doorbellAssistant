package org.mjdev.phone.ui.shape

import android.R.attr.bottom
import android.R.attr.right
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class InverseOvalShape(
    val paddingLeft: Dp = 0.dp,
    val paddingTop: Dp = 0.dp,
    val paddingRight: Dp = 0.dp,
    val paddingBottom: Dp = 0.dp,
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
            val left = paddingLeft.toPx()
            val top = paddingTop.toPx()
            val right = size.width -( paddingRight.toPx() + paddingLeft.toPx())
            val bottom = size.height - (paddingBottom.toPx() + paddingTop.toPx())
            addOval(
                Rect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                )
            )
        }.let { path ->
            Outline.Generic(path = path)
        }
    }
}
