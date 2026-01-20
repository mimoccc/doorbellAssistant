package org.mjdev.phone.vector

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toSize

class DrawCache {
    @PublishedApi
    internal var mCachedImage: ImageBitmap? = null
    private var cachedCanvas: Canvas? = null
    private var scopeDensity: Density? = null
    private var layoutDirection: LayoutDirection = LayoutDirection.Ltr
    private var size: IntSize = IntSize.Zero
    private var config: ImageBitmapConfig = ImageBitmapConfig.Argb8888
    private val cacheScope = CanvasDrawScope()

    fun drawCachedImage(
        config: ImageBitmapConfig,
        size: IntSize,
        density: Density,
        layoutDirection: LayoutDirection,
        block: DrawScope.() -> Unit,
    ) {
        this.scopeDensity = density
        this.layoutDirection = layoutDirection
        var targetImage = mCachedImage
        var targetCanvas = cachedCanvas
        if (
            targetImage == null ||
                targetCanvas == null ||
                size.width > targetImage.width ||
                size.height > targetImage.height ||
                this.config != config
        ) {
            targetImage = ImageBitmap(size.width, size.height, config = config)
            targetCanvas = Canvas(targetImage)
            mCachedImage = targetImage
            cachedCanvas = targetCanvas
            this.config = config
        }
        this.size = size
        cacheScope.draw(density, layoutDirection, targetCanvas, size.toSize()) {
            clear()
            block()
        }
        targetImage.prepareToDraw()
    }

    fun drawInto(
        target: DrawScope,
        alpha: Float = 1.0f,
        colorFilter: ColorFilter? = null
    ) {
        val targetImage = mCachedImage
        target.drawImage(targetImage!!, srcSize = size, alpha = alpha, colorFilter = colorFilter)
    }

    private fun DrawScope.clear() {
        drawRect(
            color = Color.Black,
            blendMode = BlendMode.Clear
        )
    }
}
