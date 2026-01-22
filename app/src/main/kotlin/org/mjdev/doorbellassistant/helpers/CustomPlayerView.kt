/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.lang.reflect.Array.setFloat
import android.graphics.Color as AndroidColor

@Suppress("DEPRECATION")
@OptIn(UnstableApi::class)
class CustomPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val playerView by lazy {
        runCatching {
            if (isInEditMode) null else PlayerView(context)
        }.getOrNull()
    }

    var targetColor: Color = Color(0xFF00FF00)
        set(value) {
            field = value
            chromaOverlay?.targetColor = value
        }

    var threshold: Float = 0.15f
        set(value) {
            field = value
            chromaOverlay?.threshold = value
        }

    private var chromaOverlay: ChromaKeyOverlay? = null

    var player: ExoPlayer?
        get() = playerView?.player as? ExoPlayer
        set(value) {
            runCatching { playerView?.player = value }
        }

    var useController: Boolean
        get() = playerView?.useController ?: false
        set(value) {
            runCatching { playerView?.useController = value }
        }

    var useArtwork: Boolean
        get() = playerView?.useArtwork ?: false
        set(value) {
            runCatching { playerView?.useArtwork = value }
        }

    var resizeMode: Int
        get() = playerView?.resizeMode ?: 0
        set(value) {
            runCatching { playerView?.resizeMode = value }
        }

    init {
        setBackgroundColor(AndroidColor.TRANSPARENT)
        background = AndroidColor.TRANSPARENT.toDrawable()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        playerView?.also { addView(it) }
        chromaOverlay = ChromaKeyOverlay(context).apply {
            targetColor = this@CustomPlayerView.targetColor
            threshold = this@CustomPlayerView.threshold
        }
        addView(chromaOverlay)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        chromaOverlay = null
        removeAllViews()
    }

    private inner class ChromaKeyOverlay(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var videoTexture: TextureView? = null
        private var inputBitmap: Bitmap? = null
        private var runtimeShader: RuntimeShader? = null

        private val shaderCode = """
            uniform shader inputShader;
            uniform float targetR;
            uniform float targetG;
            uniform float targetB;
            uniform float threshold;
            half4 main(float2 fragCoord) {
                half4 color = inputShader.eval(fragCoord);
                float dist = distance(color.rgb, vec3(targetR, targetG, targetB));
                float alpha = smoothstep(threshold, threshold + 0.05, dist);
                return half4(color.rgb, color.a * alpha);
            }
        """.trimIndent()

        var targetColor: Color = Color.Green
            set(value) {
                field = value
                updateShader()
            }

        var threshold: Float = 0.15f
            set(value) {
                field = value
                updateShader()
            }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            post {
                videoTexture = findTextureViewInParent()
                updateBitmapSize()
            }
        }

        private fun findTextureViewInParent(): TextureView? = (parent as? ViewGroup)?.findTextureView()

        private fun ViewGroup.findTextureView(): TextureView? {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is TextureView) return child
                if (child is ViewGroup) return child.findTextureView()
            }
            return null
        }

        private fun updateBitmapSize() {
            videoTexture ?: return
            val width = videoTexture!!.width
            val height = videoTexture!!.height
            if (width > 0 && height > 0) {
                inputBitmap?.recycle()
                inputBitmap = createBitmap(width, height)
                updateShader()
            }
        }

        @SuppressLint("NewApi")
        private fun updateShader() {
            inputBitmap ?: return
            runtimeShader = RuntimeShader(shaderCode).apply {
                setInputShader("inputShader", inputBitmap!!.toShader())
                setFloat("targetR", 0, targetColor.red)
                setFloat("targetG", 0, targetColor.green)
                setFloat("targetB", 0, targetColor.blue)
                setFloat("threshold", 0, threshold)
            }
            paint.shader = runtimeShader
        }

        @SuppressLint("DrawAllocation", "NewApi")
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            videoTexture ?: return
            inputBitmap ?: return
            runCatching {
                val glBitmap = createBitmapFromGLSurface(videoTexture!!)
                inputBitmap?.let { bitmap ->
                    bitmap.eraseColor(AndroidColor.TRANSPARENT)
                    val canvasTemp = Canvas(bitmap)
                    canvasTemp.drawBitmap(glBitmap, 0f, 0f, null)
                    glBitmap.recycle()
                }
                runtimeShader?.setInputShader("inputShader", inputBitmap!!.toShader())
                paint.shader = runtimeShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }

        @SuppressLint("NewApi")
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            updateBitmapSize()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            inputBitmap?.recycle()
            runtimeShader = null
        }

        private fun createBitmapFromGLSurface(textureView: TextureView): Bitmap {
            val bitmap = createBitmap(textureView.width.takeIf { it > 0 } ?: 1,
                textureView.height.takeIf { it > 0 } ?: 1)
            val canvas = Canvas(bitmap)
            textureView.draw(canvas)
            return bitmap
        }

        fun Bitmap.toShader(): Shader = BitmapShader(
            this, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
        )
    }
}
