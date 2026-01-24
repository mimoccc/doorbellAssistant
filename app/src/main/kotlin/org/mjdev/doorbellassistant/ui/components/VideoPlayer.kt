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
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import org.mjdev.doorbellassistant.extensions.CustomAppExt.createExoPlayer
import org.mjdev.doorbellassistant.helpers.video.ChromaTextureView
import org.mjdev.phone.extensions.ComposeExt.rememberAssetImagePainter
import org.mjdev.phone.extensions.CustomExt.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.components.ChromedImage
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Suppress("unused")
@Previews
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoUri: Uri = Uri.EMPTY,
    autoPlay: Boolean = true,
    keepAspect: Boolean = true,
    clearColor :Color = Color(0xFFFF9500),
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
    val exoPlayer = remember {
        if (isPreview) null else createExoPlayer(
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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isPreview) {
            ChromedImage(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Inside,
                painter = rememberAssetImagePainter("avatar/avatar_yellow.png"),
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    ChromaTextureView(context) {
                        // todo
                        setKeyColorRgb(
                            clearColor.red,
                            clearColor.green,
                            clearColor.blue
                        )
                        setThreshold(0.15f)
                        setSoftness(0.06f)
                        setPlayer(exoPlayer)
                    }
                },
                update = { view ->
                    with(view) {
                        this.setPlayer(exoPlayer)
                    }
                }
            )
            LaunchedEffect(videoUri) {
                if (videoUri == Uri.EMPTY) {
                    if (exoPlayer?.isPlaying==true) {
                        exoPlayer.pause()
                    }
                } else {
                    if (exoPlayer?.isPlaying==true) {
                        exoPlayer.pause()
                    }
                    exoPlayer?.setMediaItem(MediaItem.fromUri(videoUri))
                    exoPlayer?.prepare()
                    exoPlayer?.playWhenReady = autoPlay
                    if (autoPlay) {
                        exoPlayer?.play()
                    }
                }
            }
            DisposableEffect(
                Unit
            ) {
                onDispose {
                    exoPlayer?.pause()
                    exoPlayer?.release()
                }
            }
        }
    }
}