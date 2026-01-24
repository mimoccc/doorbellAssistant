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

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.media3.common.Metadata
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.enums.VideoSources
import org.mjdev.doorbellassistant.ui.components.CartoonPlayerState.Companion.rememberCartoonState
import org.mjdev.phone.extensions.CustomExt.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.components.BackgroundLayout
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Suppress("unused", "ParamsComparedByRef")
@Previews
@OptIn(UnstableApi::class)
@Composable
fun CartoonPlayer(
    modifier: Modifier = Modifier,
    state: CartoonPlayerState = rememberCartoonState(),
    keepAspect: Boolean = true,
    onVideoSizeChange: (VideoSize, Float) -> Unit = { s, f -> },
    onMetadataReceived: (Metadata) -> Unit = {},
    onFirstFrameRendered: () -> Unit = {},
    onVideoStart: (ExoPlayer) -> Unit = {},
    onVideoFinish: (ExoPlayer) -> Unit = { p -> p.pause() },
    onPaused: () -> Boolean = { true },
    onResumed: () -> Boolean = { true },
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) = PhoneTheme {
    BackgroundLayout(
        assetImageFile = "avatar/avatar_transparent.png",
        showImage = isPreview
    ) {
        VideoPlayer(
            modifier = modifier,
            keepAspect = keepAspect,
            videoUri = state.sourceUri,
            autoPlay = true,
            onCreated = { p ->
                state.exoPlayer = p
            },
            onVideoSizeChange = onVideoSizeChange,
            onMetadataReceived = onMetadataReceived,
            onFirstFrameRendered = onFirstFrameRendered,
            onVideoFinish = onVideoFinish,
            onVideoStart = onVideoStart,
            onPaused = onPaused,
            onResumed = onResumed,
        )
    }
}

@Suppress("CanBeParameter", "unused")
class CartoonPlayerState(
    val context: Context,
    var exoPlayer: ExoPlayer? = null,
    val initialSource: VideoSources = VideoSources.Welcome,
    val initialVisible: Boolean = false,
    val onVideoStart: (ExoPlayer) -> Unit = { p -> },
    val onVideoFinish: (ExoPlayer) -> Unit = { p -> },
    val onFrame: (ExoPlayer) -> Unit = {},
) {
    val videoState: MutableState<VideoSources?> = mutableStateOf(initialSource)
    val visibleState: MutableState<Boolean> = mutableStateOf(initialVisible)
    var source: VideoSources?
        get() = videoState.value
        set(value) {
            videoState.value = value
        }
    val sourceUri: Uri
        get() = source?.path?.let { p ->
            "asset:///$p"
        }?.toUri() ?: Uri.EMPTY

    @OptIn(UnstableApi::class)
    fun reset() {
        exoPlayer?.unmute()
        videoState.value = initialSource
    }

    @OptIn(UnstableApi::class)
    fun unavailable() {
        exoPlayer?.unmute()
        source = VideoSources.Unavailable
    }

    @OptIn(UnstableApi::class)
    fun warning() {
        exoPlayer?.unmute()
        source = VideoSources.Warning
    }

    @OptIn(UnstableApi::class)
    fun welcome() {
        exoPlayer?.unmute()
        source = VideoSources.Welcome
    }

    @OptIn(UnstableApi::class)
    fun onAudioRecordSent() {
        exoPlayer?.unmute()
        source = VideoSources.RecordSent
    }

    @OptIn(UnstableApi::class)
    fun onAudioRecordStart() {
        exoPlayer?.unmute()
        source = VideoSources.RecordStarting
    }

    @OptIn(UnstableApi::class)
    fun onCallStarted() {
        exoPlayer?.unmute()
        source = VideoSources.Ringing
    }

    @OptIn(UnstableApi::class)
    fun idle() {
        exoPlayer?.mute()
//        source = null // todo listening animation
    }

    fun play() {
        if (exoPlayer?.isPlaying == false) {
            exoPlayer?.play()
        }
    }

    fun pause() {
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.pause()
        }
    }

    fun stop() {
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.stop()
        }
    }

    fun pause(delay: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            pause()
            delay(delay)
            play()
        }
    }

    @OptIn(UnstableApi::class)
    fun mute() {
        exoPlayer?.mute()
    }

    @OptIn(UnstableApi::class)
    fun unmute() {
        exoPlayer?.unmute()
    }

    fun seek(position: Long) {
        exoPlayer?.seekTo(position)
    }

    companion object {
        @Suppress("unused", "ParamsComparedByRef")
        @Composable
        fun rememberCartoonState(
            initialSource: VideoSources = VideoSources.Welcome,
            initialVisible: Boolean = isPreview,
            exoPlayer: ExoPlayer? = null
        ): CartoonPlayerState {
            val context = LocalContext.current
            return remember {
                CartoonPlayerState(context, exoPlayer, initialSource, initialVisible)
            }
        }
    }
}
