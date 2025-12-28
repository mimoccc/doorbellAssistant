package org.mjdev.doorbellassistant.ui.components

import android.view.TextureView
import android.view.View
import android.view.View.LAYER_TYPE_SOFTWARE
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import org.mjdev.doorbellassistant.extensions.ComposeExt.isDesignMode
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.helpers.views.VideoTextureViewRenderer
import org.webrtc.EglBase
import org.webrtc.NetworkMonitor.init
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Previews
@Composable
fun VideoRenderer(
    modifier: Modifier = Modifier,
    videoTrack: VideoTrack? = null,
    eglBaseContext: EglBase.Context? = null,
    imageVector: ImageVector = Icons.Filled.AccountCircle,
    isDesign: Boolean = isDesignMode,
    shape: Shape = RoundedCornerShape(16.dp)
) {
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
        if (isDesignMode.not()) {
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
                imageVector = Icons.Filled.AccountCircle,
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

//@Previews
//@Composable
//fun VideoRenderer(
//    modifier: Modifier = Modifier,
//    videoTrack: VideoTrack? = null,
//    eglBaseContext: EglBase.Context? = null,
//    imageVector: ImageVector = Icons.Filled.AccountCircle,
//    isDesign: Boolean = isDesignMode,
//    shape: Shape = RoundedCornerShape(16.dp)
//) {
//    val isNotDesign by remember(isDesign) { derivedStateOf { isDesign.not() } }
//    val isVisible by remember(videoTrack) {
//        derivedStateOf {
//            videoTrack != null && !videoTrack.isDisposed
//        }
//    }
//    // todo shape
//    if (isNotDesign && isVisible) {
//        AndroidView(
//            modifier = modifier.clip(shape),
//            factory = { context ->
//                runCatching {
//                    VideoTextureViewRenderer(context).apply {
////                        setLayerType(LAYER_TYPE_SOFTWARE, null)
//                        setBackgroundColor(0)
////                        setMirror(false)
////                        setEnableHardwareScaler(false)
//                        eglBaseContext?.let { ctx ->
//                            init(ctx, object:RendererCommon.RendererEvents {
//                                override fun onFirstFrameRendered() {
//                                    TODO("Not yet implemented")
//                                }
//
//                                override fun onFrameResolutionChanged(
//                                    p0: Int,
//                                    p1: Int,
//                                    p2: Int
//                                ) {
//                                    TODO("Not yet implemented")
//                                }
//
//                            })
//                        }
//                    }
//                }.getOrNull() ?: View(context)
//            },
//            update = { v ->
//                runCatching {
//                    if (v is SurfaceViewRenderer && eglBaseContext != null && videoTrack != null) {
//                        videoTrack.addSink(v)
//                    }
//                }.onFailure { e ->
//                    e.printStackTrace()
//                }
//            }
//        )
//    }
////    if (isDesign || !isVisible) {
////        Image(
////            modifier = modifier.clip(shape),
////            imageVector = imageVector,
////            contentDescription = ""
////        )
////    }
//}
