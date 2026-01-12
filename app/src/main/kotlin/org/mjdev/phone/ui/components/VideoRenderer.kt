package org.mjdev.phone.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.viewinterop.AndroidView
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.helpers.views.VideoTextureViewRenderer
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phoneIcons
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.VideoTrack

@Previews
@Composable
fun VideoRenderer(
    modifier: Modifier = Modifier,
    videoTrack: VideoTrack? = null,
    eglBaseContext: EglBase.Context? = null,
    isDesign: Boolean = isPreview,
) = PhoneTheme {
    Box(
        modifier = modifier
    ) {
        val trackState: MutableState<VideoTrack?> = remember { mutableStateOf(null) }
        var view: VideoTextureViewRenderer? by remember { mutableStateOf(null) }
        var isFirstFrameRendered: Boolean by remember { mutableStateOf(false) }
        val isDesignOrEmpty by remember(isDesign, isFirstFrameRendered) {
            derivedStateOf {
                isDesign || isFirstFrameRendered.not()
            }
        }
        if (isPreview.not()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    VideoTextureViewRenderer(context).apply {
                        init(
                            eglBaseContext,
                            object : RendererCommon.RendererEvents {
                                override fun onFirstFrameRendered() {
                                    isFirstFrameRendered = true
                                }

                                override fun onFrameResolutionChanged(
                                    p0: Int,
                                    p1: Int,
                                    p2: Int
                                ) {
                                    isFirstFrameRendered = true
                                }
                            }
                        )
                        setupVideo(trackState, videoTrack, this)
                        view = this
                    }
                },
                update = { v ->
                    setupVideo(trackState, videoTrack, v)
                },
            )
            DisposableEffect(videoTrack) {
                onDispose {
                    cleanTrack(view, trackState)
                }
            }
        }
        if (isDesignOrEmpty) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = phoneIcons.videoCallRendererUser,
                colorFilter = ColorFilter.tint(phoneColors.colorVideoCallRendererUser),
                contentDescription = ""
            )
        }
    }
}

private fun cleanTrack(
    view: VideoTextureViewRenderer?,
    trackState: MutableState<VideoTrack?>
) {
    view?.let {
        trackState.value?.removeSink(it)
    }
    trackState.value = null
}

private fun setupVideo(
    trackState: MutableState<VideoTrack?>,
    track: VideoTrack?,
    renderer: VideoTextureViewRenderer
) {
    if (trackState.value == track) {
        return
    }
    cleanTrack(renderer, trackState)
    trackState.value = track
    track?.addSink(renderer)
}
