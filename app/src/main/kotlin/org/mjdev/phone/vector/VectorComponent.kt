package org.mjdev.phone.vector

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Size.Companion.Unspecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.DefaultGroupName
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlendModeColorFilter
import androidx.compose.ui.graphics.Color
import kotlin.math.ceil

class VectorComponent(
    val root: GroupComponent
) : VNode() {
    var name: String = DefaultGroupName
    val cacheBitmapConfig: ImageBitmapConfig
        get() = cacheDrawScope.mCachedImage?.config ?: ImageBitmapConfig.Argb8888
    var invalidateCallback = {}
    var intrinsicColorFilter: ColorFilter? by mutableStateOf(null)
    var viewportSize by mutableStateOf(Size.Zero)

    private var isDirty = true
    private val cacheDrawScope = DrawCache()
    private var tintFilter: ColorFilter? = null
    private var previousDrawSize = Unspecified
    private var rootScaleX = 1f
    private var rootScaleY = 1f

    private val drawVectorBlock: DrawScope.() -> Unit = {
        with(root) {
            scale(rootScaleX, rootScaleY, pivot = Offset.Zero) {
                draw()
            }
        }
    }

    init {
        root.invalidateListener = { doInvalidate() }
    }

    private fun doInvalidate() {
        isDirty = true
        invalidateCallback.invoke()
    }

    fun ColorFilter?.tintableWithAlphaMask() = if (this is BlendModeColorFilter) {
        this.blendMode == BlendMode.SrcIn || this.blendMode == BlendMode.SrcOver
    } else {
        this == null
    }

    fun Color.toOpaque(): Color = if (this.alpha != 1.0F) this.copy(alpha = 1.0F) else this

    fun DrawScope.draw(
        alpha: Float,
        colorFilter: ColorFilter?
    ) {
        val isOneColor = root.isTintable && root.tintColor.isSpecified
        val targetImageConfig =
            if (
                isOneColor &&
                intrinsicColorFilter.tintableWithAlphaMask() &&
                colorFilter.tintableWithAlphaMask()
            ) {
                ImageBitmapConfig.Alpha8
            } else {
                ImageBitmapConfig.Argb8888
            }
        if (isDirty || previousDrawSize != size || targetImageConfig != cacheBitmapConfig) {
            tintFilter = if (targetImageConfig == ImageBitmapConfig.Alpha8) {
                ColorFilter.tint(root.tintColor.toOpaque())
            } else {
                null
            }
            rootScaleX = size.width / viewportSize.width
            rootScaleY = size.height / viewportSize.height
            cacheDrawScope.drawCachedImage(
                targetImageConfig,
                IntSize(ceil(size.width).toInt(), ceil(size.height).toInt()),
                this@draw,
                layoutDirection,
                drawVectorBlock,
            )
            isDirty = false
            previousDrawSize = size
        }
        val targetFilter = if (colorFilter != null) {
            colorFilter
        } else if (intrinsicColorFilter != null) {
            intrinsicColorFilter
        } else {
            tintFilter
        }
        cacheDrawScope.drawInto(this, alpha, targetFilter)
    }

    override fun DrawScope.draw() {
        draw(1.0f, null)
    }

    override fun toString(): String {
        return buildString {
            append("Params: ")
            append("\tname: ").append(name).append("\n")
            append("\tviewportWidth: ").append(viewportSize.width).append("\n")
            append("\tviewportHeight: ").append(viewportSize.height).append("\n")
        }
    }
}
