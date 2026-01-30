/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.vector

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RootGroupName
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.createBitmap
import org.mjdev.phone.vector.GroupComponent.Companion.createGroupComponent

object ImageVectorProvider {

    fun createVectorPainter(
        image: ImageVector,
        density: Density = Density(1f),
    ): VectorPainter = createVectorPainterFromImageVector(
        density,
        image,
        GroupComponent().apply {
            createGroupComponent(image.root)
        },
    )

    fun Density.obtainSizePx(
        defaultWidth: Dp,
        defaultHeight: Dp
    ) = Size(defaultWidth.toPx(), defaultHeight.toPx())

    fun obtainViewportSize(
        defaultSize: Size,
        viewportWidth: Float,
        viewportHeight: Float
    ) = Size(
        if (viewportWidth.isNaN()) defaultSize.width else viewportWidth,
        if (viewportHeight.isNaN()) defaultSize.height else viewportHeight,
    )

    fun VectorPainter.configureVectorPainter(
        defaultSize: Size,
        viewportSize: Size,
        name: String = RootGroupName,
        intrinsicColorFilter: ColorFilter?,
        autoMirror: Boolean = false,
    ): VectorPainter = apply {
        this.size = defaultSize
        this.autoMirror = autoMirror
        this.intrinsicColorFilter = intrinsicColorFilter
        this.viewportSize = viewportSize
        this.name = name
    }

    fun createColorFilter(
        tintColor: Color,
        tintBlendMode: BlendMode
    ): ColorFilter? = if (tintColor.isSpecified) {
        ColorFilter.tint(tintColor, tintBlendMode)
    } else {
        null
    }

    fun createVectorPainterFromImageVector(
        density: Density,
        imageVector: ImageVector,
        root: GroupComponent,
    ): VectorPainter {
        val defaultSize = density.obtainSizePx(imageVector.defaultWidth, imageVector.defaultHeight)
        val viewport = obtainViewportSize(
            defaultSize,
            imageVector.viewportWidth,
            imageVector.viewportHeight
        )
        return VectorPainter(root).configureVectorPainter(
            defaultSize = defaultSize,
            viewportSize = viewport,
            name = imageVector.name,
            intrinsicColorFilter = createColorFilter(
                imageVector.tintColor,
                imageVector.tintBlendMode
            ),
            autoMirror = imageVector.autoMirror,
        )
    }

    fun ImageVector.toBitmap(
        density: Density = Density(1f),
        width: Int? = null,
        height:Int? = width,
        tintColor: Color = Color.Unspecified
    ): Bitmap {
        val painter = createVectorPainter(
            image = this,
            density = density
        )
        val bitmap = createBitmap(
            width ?: painter.intrinsicSize.width.toInt(),
            height ?: painter.intrinsicSize.height.toInt(),
        )
        val canvas = Canvas(bitmap)
        val size = Size(bitmap.width.toFloat(), bitmap.height.toFloat())
        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = androidx.compose.ui.graphics.Canvas(canvas),
            size = size
        ) {
            with(painter) {
                draw(
                    size = size,
                    colorFilter = ColorFilter.tint(tintColor)
                )
            }
        }
        return bitmap
    }

//    @SuppressLint("RestrictedApi")
//    @GlanceComposable
//    @Composable
//    fun rememberVectorImageProvider(
//        imageVector: ImageVector,
//        size: Dp,
//        tint: Color = Color.Unspecified,
//        density: Density = Density(1f)
//    ): State<BitmapImageProvider> {
//        val sizePx = (size.value * density.density).toInt()
//        return remember(imageVector, size, tint) {
//            derivedStateOf {
//                BitmapImageProvider(
//                    imageVector.toBitmap(
//                        density = density,
//                        sizePx = sizePx,
//                        tintColor = tint
//                    )
//                )
//            }
//        }
//    }

}