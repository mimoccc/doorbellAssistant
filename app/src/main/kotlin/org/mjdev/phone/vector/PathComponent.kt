package org.mjdev.phone.vector

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.DefaultFillType
import androidx.compose.ui.graphics.vector.DefaultPathName
import androidx.compose.ui.graphics.vector.DefaultStrokeLineCap
import androidx.compose.ui.graphics.vector.DefaultStrokeLineJoin
import androidx.compose.ui.graphics.vector.DefaultStrokeLineMiter
import androidx.compose.ui.graphics.vector.DefaultStrokeLineWidth
import androidx.compose.ui.graphics.vector.DefaultTrimPathEnd
import androidx.compose.ui.graphics.vector.DefaultTrimPathOffset
import androidx.compose.ui.graphics.vector.DefaultTrimPathStart
import androidx.compose.ui.graphics.vector.EmptyPath
import androidx.compose.ui.graphics.vector.toPath

internal class PathComponent : VNode() {
    var name = DefaultPathName
        set(value) {
            field = value
            invalidate()
        }
    var fill: Brush? = null
        set(value) {
            field = value
            invalidate()
        }
    var fillAlpha = 1.0f
        set(value) {
            field = value
            invalidate()
        }
    var pathData = EmptyPath
        set(value) {
            field = value
            isPathDirty = true
            invalidate()
        }
    var pathFillType = DefaultFillType
        set(value) {
            field = value
            renderPath.fillType = value
            invalidate()
        }
    var strokeAlpha = 1.0f
        set(value) {
            field = value
            invalidate()
        }
    var strokeLineWidth = DefaultStrokeLineWidth
        set(value) {
            field = value
            isStrokeDirty = true
            invalidate()
        }
    var stroke: Brush? = null
        set(value) {
            field = value
            invalidate()
        }
    var strokeLineCap = DefaultStrokeLineCap
        set(value) {
            field = value
            isStrokeDirty = true
            invalidate()
        }
    var strokeLineJoin = DefaultStrokeLineJoin
        set(value) {
            field = value
            isStrokeDirty = true
            invalidate()
        }
    var strokeLineMiter = DefaultStrokeLineMiter
        set(value) {
            field = value
            isStrokeDirty = true
            invalidate()
        }
    var trimPathStart = DefaultTrimPathStart
        set(value) {
            field = value
            isTrimPathDirty = true
            invalidate()
        }
    var trimPathEnd = DefaultTrimPathEnd
        set(value) {
            field = value
            isTrimPathDirty = true
            invalidate()
        }
    var trimPathOffset = DefaultTrimPathOffset
        set(value) {
            field = value
            isTrimPathDirty = true
            invalidate()
        }

    private var isPathDirty = true
    private var isStrokeDirty = true
    private var isTrimPathDirty = false
    private var strokeStyle: Stroke? = null
    private val path = Path()
    private var renderPath = path

    private val pathMeasure: PathMeasure by lazy(LazyThreadSafetyMode.NONE) {
        PathMeasure()
    }

    private fun updatePath() {
        pathData.toPath(path)
        updateRenderPath()
    }

    private fun updateRenderPath() {
        if (trimPathStart == DefaultTrimPathStart && trimPathEnd == DefaultTrimPathEnd) {
            renderPath = path
        } else {
            if (renderPath == path) {
                renderPath = Path()
            } else {
                val fillType = renderPath.fillType
                renderPath.rewind()
                renderPath.fillType = fillType
            }
            pathMeasure.setPath(path, false)
            val length = pathMeasure.length
            val start = ((trimPathStart + trimPathOffset) % 1f) * length
            val end = ((trimPathEnd + trimPathOffset) % 1f) * length
            if (start > end) {
                pathMeasure.getSegment(
                    start,
                    length,
                    renderPath,
                    true
                )
                pathMeasure.getSegment(
                    0f,
                    end,
                    renderPath,
                    true
                )
            } else {
                pathMeasure.getSegment(
                    start,
                    end,
                    renderPath,
                    true
                )
            }
        }
    }

    override fun DrawScope.draw() {
        if (isPathDirty) {
            updatePath()
        } else if (isTrimPathDirty) {
            updateRenderPath()
        }
        isPathDirty = false
        isTrimPathDirty = false
        fill?.let { drawPath(renderPath, brush = it, alpha = fillAlpha) }
        stroke?.let {
            var targetStroke = strokeStyle
            if (isStrokeDirty || targetStroke == null) {
                targetStroke = Stroke(strokeLineWidth, strokeLineMiter, strokeLineCap, strokeLineJoin)
                strokeStyle = targetStroke
                isStrokeDirty = false
            }
            drawPath(renderPath, brush = it, alpha = strokeAlpha, style = targetStroke)
        }
    }

    override fun toString() = path.toString()
}