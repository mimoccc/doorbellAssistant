package org.mjdev.phone.vector

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.vector.DefaultGroupName
import androidx.compose.ui.graphics.vector.DefaultPivotX
import androidx.compose.ui.graphics.vector.DefaultPivotY
import androidx.compose.ui.graphics.vector.DefaultRotation
import androidx.compose.ui.graphics.vector.DefaultScaleX
import androidx.compose.ui.graphics.vector.DefaultScaleY
import androidx.compose.ui.graphics.vector.DefaultTranslationX
import androidx.compose.ui.graphics.vector.DefaultTranslationY
import androidx.compose.ui.graphics.vector.EmptyPath
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.util.fastForEach

@Suppress("unused")
class GroupComponent : VNode() {
    private var groupMatrix: Matrix? = null
    private val children = mutableListOf<VNode>()

    var isTintable = true
        private set

    var tintColor = Color.Unspecified
        private set

    var clipPathData = EmptyPath
        set(value) {
            field = value
            isClipPathDirty = true
            invalidate()
        }

    private val willClipPath: Boolean
        get() = clipPathData.isNotEmpty()

    private var isClipPathDirty = true

    private var clipPath: Path? = null

    override var invalidateListener: ((VNode) -> Unit)? = null

    var name = DefaultGroupName
        set(value) {
            field = value
            invalidate()
        }

    var rotation = DefaultRotation
        set(value) {
            field = value
            isMatrixDirty = true
            invalidate()
        }

    var pivotX = DefaultPivotX
        set(value) {
            field = value
            isMatrixDirty = true
            invalidate()
        }

    var pivotY = DefaultPivotY
        set(value) {
            field = value
            isMatrixDirty = true
            invalidate()
        }

    var scaleX = DefaultScaleX
        set(value) {
            field = value
            isMatrixDirty = true
            invalidate()
        }

    var scaleY = DefaultScaleY
        set(value) {
            field = value
            isMatrixDirty = true
            invalidate()
        }

    var translationX = DefaultTranslationX
        set(value) {
            field = value
            isMatrixDirty = true
            invalidate()
        }

    var translationY = DefaultTranslationY
        set(value) {
            field = value
            isMatrixDirty = true
            invalidate()
        }

    val numChildren: Int
        get() = children.size

    private var isMatrixDirty = true

    private val wrappedListener: (VNode) -> Unit = { node ->
        markTintForVNode(node)
        invalidateListener?.invoke(node)
    }

    private fun markTintForBrush(brush: Brush?) {
        if (!isTintable) {
            return
        }
        if (brush != null) {
            if (brush is SolidColor) {
                markTintForColor(brush.value)
            } else {
                markNotTintable()
            }
        }
    }

    private fun markTintForColor(color: Color) {
        if (!isTintable) {
            return
        }
        if (color.isSpecified) {
            if (tintColor.isUnspecified) {
                tintColor = color
            } else if (!tintColor.rgbEqual(color)) {
                markNotTintable()
            }
        }
    }

    private fun markTintForVNode(node: VNode) {
        if (node is PathComponent) {
            markTintForBrush(node.fill)
            markTintForBrush(node.stroke)
        } else if (node is GroupComponent) {
            if (node.isTintable && isTintable) {
                markTintForColor(node.tintColor)
            } else {
                markNotTintable()
            }
        }
    }

    private fun markNotTintable() {
        isTintable = false
        tintColor = Color.Unspecified
    }

    private fun updateClipPath() {
        if (willClipPath) {
            var targetClip = clipPath
            if (targetClip == null) {
                targetClip = Path()
                clipPath = targetClip
            }
            clipPathData.toPath(targetClip)
        }
    }

    private fun updateMatrix() {
        val matrix: Matrix
        val target = groupMatrix
        if (target == null) {
            matrix = Matrix()
            groupMatrix = matrix
        } else {
            matrix = target
            matrix.reset()
        }
        matrix.translate(translationX + pivotX, translationY + pivotY)
        matrix.rotateZ(degrees = rotation)
        matrix.scale(scaleX, scaleY, 1f)
        matrix.translate(-pivotX, -pivotY)
    }

    fun insertAt(index: Int, instance: VNode) {
        if (index < numChildren) {
            children[index] = instance
        } else {
            children.add(instance)
        }
        markTintForVNode(instance)
        instance.invalidateListener = wrappedListener
        invalidate()
    }

    fun move(from: Int, to: Int, count: Int) {
        if (from > to) {
            var current = to
            repeat(count) {
                val node = children[from]
                children.removeAt(from)
                children.add(current, node)
                current++
            }
        } else {
            repeat(count) {
                val node = children[from]
                children.removeAt(from)
                children.add(to - 1, node)
            }
        }
        invalidate()
    }

    fun remove(index: Int, count: Int) {
        repeat(count) {
            if (index < children.size) {
                children[index].invalidateListener = null
                children.removeAt(index)
            }
        }
        invalidate()
    }

    override fun DrawScope.draw() {
        if (isMatrixDirty) {
            updateMatrix()
            isMatrixDirty = false
        }
        if (isClipPathDirty) {
            updateClipPath()
            isClipPathDirty = false
        }
        withTransform({
            groupMatrix?.let { transform(it) }
            val targetClip = clipPath
            if (willClipPath && targetClip != null) {
                clipPath(targetClip)
            }
        }) {
            children.fastForEach { node -> with(node) { this@draw.draw() } }
        }
    }

    override fun toString(): String {
        val sb = StringBuilder().append("VGroup: ").append(name)
        children.fastForEach { node -> sb.append("\t").append(node.toString()).append("\n") }
        return sb.toString()
    }

    companion object {
        fun GroupComponent.createGroupComponent(
            currentGroup: VectorGroup
        ): GroupComponent {
            for (index in 0 until currentGroup.size) {
                val vectorNode = currentGroup[index]
                if (vectorNode is VectorPath) {
                    val pathComponent =
                        PathComponent().apply {
                            pathData = vectorNode.pathData
                            pathFillType = vectorNode.pathFillType
                            name = vectorNode.name
                            fill = vectorNode.fill
                            fillAlpha = vectorNode.fillAlpha
                            stroke = vectorNode.stroke
                            strokeAlpha = vectorNode.strokeAlpha
                            strokeLineWidth = vectorNode.strokeLineWidth
                            strokeLineCap = vectorNode.strokeLineCap
                            strokeLineJoin = vectorNode.strokeLineJoin
                            strokeLineMiter = vectorNode.strokeLineMiter
                            trimPathStart = vectorNode.trimPathStart
                            trimPathEnd = vectorNode.trimPathEnd
                            trimPathOffset = vectorNode.trimPathOffset
                        }
                    insertAt(index, pathComponent)
                } else if (vectorNode is VectorGroup) {
                    val groupComponent = GroupComponent().apply {
                        name = vectorNode.name
                        rotation = vectorNode.rotation
                        scaleX = vectorNode.scaleX
                        scaleY = vectorNode.scaleY
                        translationX = vectorNode.translationX
                        translationY = vectorNode.translationY
                        pivotX = vectorNode.pivotX
                        pivotY = vectorNode.pivotY
                        clipPathData = vectorNode.clipPathData
                        createGroupComponent(vectorNode)
                    }
                    insertAt(index, groupComponent)
                }
            }
            return this
        }

        fun Color.rgbEqual(
            other: Color
        ) = this.red == other.red && this.green == other.green && this.blue == other.blue
    }
}
