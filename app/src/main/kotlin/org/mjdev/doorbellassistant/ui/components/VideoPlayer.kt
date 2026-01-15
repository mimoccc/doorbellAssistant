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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import org.mjdev.doorbellassistant.extensions.ComposeExt.createExoPlayer
import org.mjdev.phone.extensions.CustomExtensions.applyIf
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.extensions.CustomExtensions.rememberAssetImagePainter
import org.mjdev.phone.extensions.CustomExtensions.rememberLifeCycleOwnerState
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.helpers.views.CustomPlayerView
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Suppress("unused")
@Previews
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoUri: Uri = Uri.EMPTY,
    autoPlay: Boolean= true,
    showControls: Boolean = false,
    keepAspect: Boolean = false,
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
//    val lifecycleOwner by rememberLifeCycleOwnerState()
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
        ).apply{
            onCreated(this)
        }
    }
//    val isPlaying by remember(exoPlayer.isPlaying) {
//        derivedStateOf {
//            exoPlayer.isPlaying
//        }
//    }
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
            painter = rememberAssetImagePainter("avatar_transparent.png"),
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
//        DisposableEffect(
//            lifecycleOwner
//        ) {
//            val lifecycle = lifecycleOwner?.lifecycle
//            val observer = LifecycleEventObserver { _, event ->
//                when (event) {
//                    Lifecycle.Event.ON_RESUME -> {
//                        if (onResumed()) {
//                            if (!isPlaying) exoPlayer.play()
//                        }
//                    }
//
//                    Lifecycle.Event.ON_PAUSE -> {
//                        if (onPaused()) {
//                            if (isPlaying) exoPlayer.stop()
//                        }
//                    }
//
//                    Lifecycle.Event.ON_DESTROY -> {
//                        exoPlayer.release()
//                    }
//
//                    else -> Unit
//                }
//            }
//            lifecycle?.addObserver(observer)
//            onDispose {
//                exoPlayer.stop()
//                exoPlayer.release()
//                lifecycle?.removeObserver(observer)
//            }
//        }
    }
}
