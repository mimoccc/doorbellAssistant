/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package org.mjdev.doorbellassistant.helpers.video

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import androidx.annotation.MainThread
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import java.util.concurrent.atomic.AtomicBoolean

class ChromaTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    config: ChromaTextureView.() -> Unit = {},
) : TextureView(context, attrs), TextureView.SurfaceTextureListener {

    private var player: ExoPlayer? = null
    private var renderThread: ChromaRenderThread? = null
    private val started = AtomicBoolean(false)
    private val listeners = mutableListOf<ChromaTextureViewListener>()

    private val playerListener = object : Player.Listener {
        override fun onVideoSizeChanged(size: VideoSize) {
            renderThread?.setVideoSize(
                w = size.width,
                h = size.height
            )

            val aspect =
                if (size.width > 0 && size.height > 0) {
                    size.width.toFloat() / size.height.toFloat()
                } else 1f

            onVideoSizeChange(size, aspect)
        }
    }

    init {
        surfaceTextureListener = this
        isOpaque = false
        config(this)
    }

    @MainThread
    fun setPlayer(
        p: ExoPlayer?
    ) {
        if (player === p) {
            renderThread?.inputSurface?.let { s ->
                player?.setVideoSurface(s)
            }
            return
        }

        player?.removeListener(playerListener)
        player?.setVideoSurface(null)

        player = p

        player?.addListener(playerListener)

        renderThread?.inputSurface?.let { s ->
            player?.setVideoSurface(s)
        }
    }

    private fun onVideoSizeChange(
        size: VideoSize,
        aspect: Float
    ) {
        listeners.onEach {
            onVideoSizeChange(size, aspect)
        }
    }

    fun setKeyColorRgb(r: Float, g: Float, b: Float) {
        renderThread?.setKeyColor(r, g, b)
    }

    fun setThreshold(t: Float) {
        renderThread?.setThreshold(t)
    }

    fun setSoftness(s: Float) {
        renderThread?.setSoftness(s)
    }

    override fun onSurfaceTextureAvailable(
        st: SurfaceTexture,
        w: Int,
        h: Int
    ) {
        if (started.getAndSet(true)) return

        val outSurface = Surface(st)

        val t = ChromaRenderThread(
            outputSurface = outSurface,
            outputWidth = w,
            outputHeight = h,
            onInputSurfaceReady = { inputSurface ->
                post {
                    player?.setVideoSurface(inputSurface)
                }
            }
        )

        renderThread = t
        t.start()
    }

    override fun onSurfaceTextureSizeChanged(
        st: SurfaceTexture,
        w: Int,
        h: Int
    ) {
        renderThread?.setOutputSize(w, h)
    }

    override fun onSurfaceTextureDestroyed(
        st: SurfaceTexture
    ): Boolean {
        started.set(false)

        player?.setVideoSurface(null)
        player?.removeListener(playerListener)

        renderThread?.shutdown()
        renderThread = null

        return true
    }

    override fun onSurfaceTextureUpdated(
        st: SurfaceTexture
    ) = Unit

    private fun <T> List<T>.onEach(
        block: T.() -> Unit
    ) = forEach { block(it) }

    fun addListener(
        chromaTextureViewListener: ChromaTextureViewListener
    ) {
        listeners.add(chromaTextureViewListener)
    }

    fun addVideoSizeListener(
        block: (size: VideoSize, aspect: Float) -> Unit
    ) {
        addListener(object : ChromaTextureViewListener() {
            override fun onVideoSizeChange(size: VideoSize, aspect: Float) {
                block(size, aspect)
            }
        })
    }

    fun removeListener(
        chromaTextureViewListener: ChromaTextureViewListener
    ) {
        listeners.remove(chromaTextureViewListener)
    }
}
