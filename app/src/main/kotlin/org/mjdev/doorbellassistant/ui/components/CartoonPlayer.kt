package org.mjdev.doorbellassistant.ui.components

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import org.mjdev.doorbellassistant.enums.VideoSources
import org.mjdev.doorbellassistant.extensions.ComposeExt.rememberExoPlayer
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.components.BackgroundLayout
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Suppress("unused")
@Previews
@OptIn(UnstableApi::class)
@Composable
fun CartoonPlayer(
    modifier: Modifier = Modifier,
    state: MutableState<VideoSources?> = mutableStateOf(null),
    keepAspect: Boolean = false,
    onVideoSizeChange: (VideoSize, Float) -> Unit = { s, f -> },
    onMetadataReceived: (Metadata) -> Unit = {},
    onFirstFrameRendered: () -> Unit = {},
    onVideoFinish: (ExoPlayer) -> Unit = { p -> p.pause() },
    onPaused: () -> Boolean = { true },
    onResumed: () -> Boolean = { true },
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) = PhoneTheme {
    val exoPlayer = rememberExoPlayer(
        videoUri = "asset:///${state.value?.path}".toUri(),
        startOnReady = true,
        repeatMode = Player.REPEAT_MODE_ALL,
        onVideoFinish = onVideoFinish,
    )
    AnimatedVisibility(
        visible = (state.value == null)
    ) {
        BackgroundLayout(
            assetImageFile = "avatar_transparent.png",
        )
    }
    AnimatedVisibility(
        visible = (state.value != null)
    ) {
        VideoPlayer(
            modifier = modifier,
            keepAspect = keepAspect,
            exoPlayer = exoPlayer,
            onVideoSizeChange = onVideoSizeChange,
            onMetadataReceived = onMetadataReceived,
            onFirstFrameRendered = onFirstFrameRendered,
            onPaused = onPaused,
            onResumed = onResumed,
        )
    }
}
