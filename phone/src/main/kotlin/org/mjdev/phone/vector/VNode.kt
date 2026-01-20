package org.mjdev.phone.vector

import androidx.compose.ui.graphics.drawscope.DrawScope

sealed class VNode {
    open var invalidateListener: ((VNode) -> Unit)? = null

    fun invalidate() {
        invalidateListener?.invoke(this)
    }

    abstract fun DrawScope.draw()
}