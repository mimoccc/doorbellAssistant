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

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.C.VIDEO_SCALING_MODE_DEFAULT
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import org.mjdev.phone.extensions.ModifierExt.applyIf

@UnstableApi
@Composable
fun VideoPlayer1(
    modifier: Modifier = Modifier,
    videoUri: Uri = Uri.EMPTY,
    autoPlay: Boolean = true,
    keepAspect: Boolean = true,
    videoScaleMode: Int = VIDEO_SCALING_MODE_DEFAULT,
    startTime: Long = 0L,
    onVideoSizeChange: (VideoSize, Float) -> Unit = { _, _ -> },
    onMetadataReceived: (Metadata) -> Unit = {},
    onFirstFrameRendered: () -> Unit = {},
    onCreated: (ExoPlayer) -> Unit = {},
    onVideoStart: (ExoPlayer) -> Unit = {},
    onVideoFinish: (ExoPlayer) -> Unit = { p -> p.pause() },
    onFrame: (ExoPlayer) -> Unit = {},
    onPaused: () -> Boolean = { true }, // todo?, may be state
    onResumed: () -> Boolean = { true }, //todo?, may be state
) {
    val context = LocalContext.current
    val aspectRatio by remember {
        mutableFloatStateOf(1f)
    }
    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context)
            .setVideoScalingMode(videoScaleMode)
            .build()
            .apply {
                onCreated(this)
                if (videoUri != Uri.EMPTY) {
                    setMediaItem(MediaItem.fromUri(videoUri))
                    prepare()
                }
                addListener(object : Player.Listener {
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
                        onVideoStart(this@apply)
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                if (currentPosition < startTime) {
                                    seekTo(startTime)
                                }
                            }
                            Player.STATE_ENDED -> {
                                onVideoFinish(this@apply)
                                onFrame(this@apply)
                            }
                            else -> {
                                // no op
                            }
                        }
                    }
                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        val isDiscontinuity = reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION
                        if (isDiscontinuity) {
                            onVideoFinish(this@apply)
                            onFrame(this@apply)
                        }
                    }
                })
                setVideoFrameMetadataListener { _, _, _, _ ->
                    onFrame(this@apply)
                }
                playWhenReady = autoPlay
            }
    }
    Column(
        modifier.fillMaxSize()
    ) {
        PlayerSurface(
            player = exoPlayer,
            modifier = Modifier
                .fillMaxWidth()
                .applyIf(keepAspect) {
                    aspectRatio(aspectRatio)
                }
                .applyIf(!keepAspect) {
                    fillMaxHeight()
                }
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}
