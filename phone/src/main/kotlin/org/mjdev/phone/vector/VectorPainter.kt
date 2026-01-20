package org.mjdev.phone.vector

import androidx.compose.runtime.Composition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.LayoutDirection

@Suppress("unused")
class VectorPainter(
    root: GroupComponent = GroupComponent()
) : Painter() {
    var size by mutableStateOf(Size.Zero)
    var autoMirror by mutableStateOf(false)
    var intrinsicColorFilter: ColorFilter?
        get() = vector.intrinsicColorFilter
        set(value) {
            vector.intrinsicColorFilter = value
        }
    var viewportSize: Size
        get() = vector.viewportSize
        set(value) {
            vector.viewportSize = value
        }
    var name: String
        get() = vector.name
        set(value) {
            vector.name = value
        }
    val vector = VectorComponent(root).apply {
        invalidateCallback = {
            if (drawCount == invalidateCount) {
                invalidateCount++
            }
        }
    }
    val bitmapConfig: ImageBitmapConfig
        get() = vector.cacheBitmapConfig
    var composition: Composition? = null
    var invalidateCount by mutableIntStateOf(0)
    var currentAlpha: Float = 1.0f
    var currentColorFilter: ColorFilter? = null
    var drawCount = -1

    override val intrinsicSize: Size
        get() = size

    fun DrawScope.mirror(block: DrawScope.() -> Unit) {
        scale(-1f, 1f, block = block)
    }

    override fun DrawScope.onDraw() {
        with(vector) {
            val filter = currentColorFilter ?: intrinsicColorFilter
            if (autoMirror && layoutDirection == LayoutDirection.Rtl) {
                mirror {
                    draw(currentAlpha, filter)
                }
            } else {
                draw(currentAlpha, filter)
            }
        }
        drawCount = invalidateCount
    }

    override fun applyAlpha(alpha: Float): Boolean {
        currentAlpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        currentColorFilter = colorFilter
        return true
    }
}
