package org.mjdev.doorbellassistant.helpers.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.core.graphics.drawable.toDrawable
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

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

    var player: ExoPlayer?
        get() = playerView?.player as? ExoPlayer
        set(value) {
            runCatching {
                playerView?.player = value
            }
        }

    var useController: Boolean
        get() = playerView?.useController ?: false
        set(value) {
            runCatching {
                playerView?.useController = value
            }
        }

    var useArtwork: Boolean
        get() = playerView?.useArtwork ?: false
        set(value) {
            runCatching {
                playerView?.useArtwork = value
            }
        }

    var resizeMode: Int
        get() = playerView?.resizeMode ?: 0
        set(value) {
            runCatching {
                playerView?.resizeMode = value
            }
        }

    init {
        setBackgroundColor(Color.TRANSPARENT)
        background = Color.TRANSPARENT.toDrawable()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        playerView?.also {
            addView(playerView)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeAllViews()
    }
}