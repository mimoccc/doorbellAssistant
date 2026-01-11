package org.mjdev.phone.vector

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
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
        density: Density,
        sizePx: Int,
        tintColor: Color = Color.Unspecified
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            sizePx,
            sizePx,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val size = Size(sizePx.toFloat(), sizePx.toFloat())
        val painter = createVectorPainter(
            image = this@toBitmap,
            density = density
        )
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