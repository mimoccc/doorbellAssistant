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

import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Suppress("DEPRECATION", "unused")
object CustomExt {
    val EmptyBitmap = createBitmap(1, 1)

    val isPreview
        get() = isLayoutLib()

    val isInPreviewMode: Boolean
        get() = isLayoutLib()

    // todo this may change, should be improved
    fun isLayoutLib(): Boolean {
        val device = Build.DEVICE
        val product = Build.PRODUCT
        return (device == "layoutlib") || (product == "layoutlib")
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun postDelayed(
        timeout: Long,
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        scopeContext: CoroutineContext,
        block: suspend () -> Unit,
    ) = scope.launch(scopeContext) {
        delay(timeout)
        block()
    }

    fun postDelayed(
        timeout: Long,
        block: () -> Unit,
    ) {
        Handler().postDelayed(block, timeout)
    }

    fun Painter.toImageBitmap(
        size: Size,
        density: Density,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ): ImageBitmap {
        val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
        val canvas = Canvas(bitmap)
        CanvasDrawScope().draw(density, layoutDirection, canvas, size) {
            draw(size)
        }
        return bitmap
    }

    fun Uri.addUriParameter(key: String, newValue: String): Uri {
        val params = queryParameterNames
        val newUri = buildUpon().clearQuery()
        var isSameParamPresent = false
        for (param in params) {
            newUri.appendQueryParameter(
                param,
                if (param == key) newValue else getQueryParameter(param)
            )
            if (param == key) {
                isSameParamPresent = true
            }
        }
        if (!isSameParamPresent) {
            newUri.appendQueryParameter(
                key,
                newValue
            )
        }
        return newUri.build()
    }


    fun LifecycleOwner.launchOnLifecycle(
        scope: LifecycleCoroutineScope = lifecycleScope,
        context: CoroutineContext = Dispatchers.Main,
        block: suspend CoroutineScope.() -> Unit
    ) = scope.launch(
        context = context,
        block = block
    )

    fun ContentDrawScope.drawNeonStroke(
        color: Color = Color.White,
        backgroundColor: Color = Color.Black,
        radius: Dp = 4.dp,
        glowAlpha: Float = 1f,
        shape: Shape
    ) {
        val outline = shape.createOutline(size, layoutDirection, this)
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                style = PaintingStyle.Stroke
                strokeWidth = 1.5f * radius.value
            }
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = color.copy(alpha = 0f).toArgb()
            frameworkPaint.setShadowLayer(
                radius.toPx(),
                0f,
                0f,
                color.copy(alpha = glowAlpha).toArgb()
            )
            canvas.drawOutline(outline, paint)
            drawIntoCanvas { canvas ->
                canvas.drawOutline(
                    outline = outline,
                    paint = Paint().apply {
                        this.color = backgroundColor
                        style = PaintingStyle.Fill
                    }
                )
            }
        }
    }
}
