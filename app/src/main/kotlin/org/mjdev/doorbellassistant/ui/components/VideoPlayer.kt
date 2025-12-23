package org.mjdev.doorbellassistant.ui.components

import android.graphics.Color
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import org.mjdev.doorbellassistant.extensions.ComposeExt.applyIf
import org.mjdev.doorbellassistant.extensions.ComposeExt.rememberExoPlayer
import org.mjdev.doorbellassistant.helpers.CustomPlayerView
import org.mjdev.doorbellassistant.helpers.Previews

@Suppress("unused")
@Previews
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer? = rememberExoPlayer(),
    showControls: Boolean = false,
    keepAspect: Boolean = false,
    onVideoSizeChange: (VideoSize, Float) -> Unit = { s, f -> },
    onMetadataReceived: (Metadata) -> Unit = {},
    onFirstFrameRendered: () -> Unit = {},
    onPaused: () -> Boolean = { true },
    onResumed: () -> Boolean = { true }
) {
    val context = LocalContext.current
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    val videoSize = remember { mutableStateOf<VideoSize?>(null) }
    val aspectRatio = remember { mutableFloatStateOf(1f) }
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .applyIf(keepAspect) {
                aspectRatio(aspectRatio.floatValue)
            }
            .applyIf(!keepAspect) {
                fillMaxHeight()
            },
        factory = {
            CustomPlayerView(context).apply {
                background = Color.TRANSPARENT.toDrawable()
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                useArtwork = true
                exoPlayer?.addListener(object : Player.Listener {
                    override fun onVideoSizeChanged(size: VideoSize) {
                        val aspect = if (size.width > 0 && size.height > 0) {
                            size.width.toFloat() / size.height.toFloat()
                        } else 1.0f
                        videoSize.value = size
                        onVideoSizeChange(size, aspect)
                    }

                    override fun onMetadata(metadata: Metadata) {
                        onMetadataReceived(metadata)
                    }

                    override fun onRenderedFirstFrame() {
                        onFirstFrameRendered()
                    }
                })
            }
        },
        update = { view ->
            view.player = exoPlayer
            view.useController = showControls
        }
    )
    DisposableEffect(exoPlayer) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (onResumed()) {
                        exoPlayer?.play()
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    if (onPaused()) {
                        exoPlayer?.pause()
                    }
                }

                Lifecycle.Event.ON_DESTROY -> exoPlayer?.release()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            exoPlayer?.stop()
            lifecycle.removeObserver(observer)
        }
    }
}
