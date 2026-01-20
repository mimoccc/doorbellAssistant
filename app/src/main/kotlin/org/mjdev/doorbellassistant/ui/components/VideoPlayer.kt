/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.ui.components

import android.graphics.Color
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import org.mjdev.doorbellassistant.extensions.CustomAppExt.createExoPlayer
import org.mjdev.doorbellassistant.helpers.CustomPlayerView
import org.mjdev.phone.extensions.ComposeExt.rememberAssetImagePainter
import org.mjdev.phone.extensions.CustomExt.isPreview
import org.mjdev.phone.extensions.ModifierExt.applyIf
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Suppress("unused")
@Previews
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoUri: Uri = Uri.EMPTY,
    autoPlay: Boolean = true,
    showControls: Boolean = false,
    keepAspect: Boolean = true,
    onVideoSizeChange: (VideoSize, Float) -> Unit = { s, f -> },
    onMetadataReceived: (Metadata) -> Unit = {},
    onFirstFrameRendered: () -> Unit = {},
    onCreated: (ExoPlayer) -> Unit = {},
    onVideoStart: (ExoPlayer) -> Unit = {},
    onVideoFinish: (ExoPlayer) -> Unit = { p -> p.pause() },
    onFrame: (ExoPlayer) -> Unit = {},
    onPaused: () -> Boolean = { true },
    onResumed: () -> Boolean = { true },
) = PhoneTheme {
    val context = LocalContext.current
    val aspectRatio by remember {
        mutableFloatStateOf(1f)
    }
    val exoPlayer = remember {
        createExoPlayer(
            context = context,
            videoUri = Uri.EMPTY,
            startOnReady = autoPlay,
            repeatMode = Player.REPEAT_MODE_ALL,
            onVideoStart = onVideoStart,
            onVideoFinish = onVideoFinish,
            onFrame = onFrame,
        ).apply {
            onCreated(this)
        }
    }
    if (isPreview) {
        Image(
            modifier = modifier
                .fillMaxWidth()
                .applyIf(keepAspect) {
                    aspectRatio(aspectRatio)
                }
                .applyIf(!keepAspect) {
                    fillMaxHeight()
                },
            painter = rememberAssetImagePainter("avatar/avatar_transparent.png"),
            contentDescription = ""
        )
    } else {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .applyIf(keepAspect) {
                    aspectRatio(aspectRatio)
                }
                .applyIf(!keepAspect) {
                    fillMaxHeight()
                },
            factory = {
                CustomPlayerView(context).apply {
                    background = Color.TRANSPARENT.toDrawable()
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    useArtwork = autoPlay
                    exoPlayer.addListener(object : Player.Listener {
                        override fun onVideoSizeChanged(size: VideoSize) {
                            val aspect = if (size.width > 0 && size.height > 0) {
                                size.width.toFloat() / size.height.toFloat()
                            } else 1.0f
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
                with(view) {
                    this.player = exoPlayer
                    this.useController = showControls
                }
            }
        )
        LaunchedEffect(videoUri) {
            if (videoUri == Uri.EMPTY) {
                if (exoPlayer.isPlaying) {
                    exoPlayer.stop()
                }
            } else {
                if (exoPlayer.isPlaying) {
                    exoPlayer.stop()
                }
                exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
                exoPlayer.prepare()
            }
        }
        DisposableEffect(
            Unit
        ) {
            onDispose {
                exoPlayer.stop()
                exoPlayer.release()
            }
        }
    }
}
