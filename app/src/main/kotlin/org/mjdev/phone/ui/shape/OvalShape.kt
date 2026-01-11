package org.mjdev.phone.ui.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

val OvalShape = object: Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Path().apply {
        addOval(
            Rect(
                left = size.width,
                top = 0f,
                right = 0f,
                bottom = size.height
            )
        )
    }.let { path ->
        Outline.Generic(path = path)
    }
}
