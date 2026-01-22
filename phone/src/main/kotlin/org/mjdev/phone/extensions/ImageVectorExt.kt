/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.extensions

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.view.View
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.createBitmap

object ImageVectorExt {
    fun ImageVector.toDrawable(
        context: Context,
        width: Int,
        height: Int
    ): BitmapDrawable {
        val computedWidth = if (width < 1) 1 else width
        val computedHeight = if (height < 1) 1 else height
        val bitmap = createBitmap(computedWidth, computedHeight)
        val canvas = Canvas(bitmap)
        val vectorDrawable = VectorDrawable()
        vectorDrawable.setBounds(0, 0, computedWidth, computedHeight)
        vectorDrawable.draw(canvas)
        return BitmapDrawable(context.resources, bitmap)
    }

    fun ImageVector.toDrawable(
        view: View,
        width: Int = view.width,
        height: Int = view.height
    ): BitmapDrawable {
        val computedWidth = if (width < 1) 1 else width
        val computedHeight = if (height < 1) 1 else height
        val bitmap = createBitmap(computedWidth, computedHeight)
        val canvas = Canvas(bitmap)
        val vectorDrawable = VectorDrawable()
        vectorDrawable.setBounds(0, 0, computedWidth, computedHeight)
        vectorDrawable.draw(canvas)
        return BitmapDrawable(view.context.resources, bitmap)
    }
}
