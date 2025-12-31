package org.mjdev.doorbellassistant.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.extensions.ComposeExt.isDesignMode
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.stream.CallEndReason
import org.mjdev.doorbellassistant.stream.CallManager
import org.mjdev.doorbellassistant.ui.screens.IncomingCallScreen
import org.mjdev.doorbellassistant.ui.theme.Callee
import org.mjdev.doorbellassistant.ui.theme.Caller
import org.mjdev.doorbellassistant.ui.theme.Controls
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack

@Previews
@Composable
fun VideoCall(
    modifier: Modifier = Modifier,
    callerDevice: NsdDevice? = NsdDevice.EMPTY,
    calleeDevice: NsdDevice? = NsdDevice.EMPTY,
    calleeVisible: Boolean = true,
    callerVisible: Boolean = true,
    callControlsVisible: Boolean = true,
    autoAnswerCall: Boolean = false,
    callerAlignment: Alignment = Alignment.Center,
    calleeAlignment: Alignment = Alignment.BottomEnd,
    controlsAlignment: Alignment = Alignment.BottomCenter,
    callerAspectRatio: Float = 0.4f,
    calleeAspectRatio: Float = 0.98f,
    callerPadding: PaddingValues = PaddingValues(0.dp),
    calleePadding: PaddingValues = PaddingValues(
        end = 16.dp,
        bottom = 16.dp
    ),
    controlsPadding: PaddingValues = PaddingValues(16.dp),
    callerBackgroundColor: Color = Caller,
    calleeBackgroundColor: Color = Callee,
    controlsBackgroundColor: Color = Controls,
    callerShape: Shape = RoundedCornerShape(16.dp),
    calleeShape: Shape = RoundedCornerShape(16.dp),
    controlsShape: Shape = CircleShape,
    onStartCall: (SessionDescription) -> Unit = {},
    onEndCall: (CallEndReason) -> Unit = {},
) {
    var localVideoTrack by remember { mutableStateOf<VideoTrack?>(null) }
    var remoteVideoTrack by remember { mutableStateOf<VideoTrack?>(null) }
    var isAccepted by remember { mutableStateOf(autoAnswerCall) }
    val rtcManager = rememberRtcManager(
        callerIp = callerDevice?.address,
        onLocalTrackReady = { track ->
            localVideoTrack = track
        },
        onRemoteTrackReady = { track ->
            remoteVideoTrack = track
        },
        onAcceptCall = {
            unmute()
            isAccepted = true
        },
        onCallStarted = { sdp ->
            onStartCall(sdp)
        },
        onCallEnded = { reason ->
            remoteVideoTrack = null
            onEndCall(reason)
        }
    )
    Box(modifier = modifier) {
        if (callerVisible) {
            Box(
                modifier = Modifier
                    .padding(callerPadding)
                    .fillMaxSize(calleeAspectRatio)
                    .clip(callerShape)
                    .background(callerBackgroundColor, callerShape)
                    .align(callerAlignment)
            ) {
                VideoRenderer(
                    modifier = Modifier.fillMaxSize(),
                    videoTrack = remoteVideoTrack,
                    eglBaseContext = rtcManager?.eglBaseContext,
                    shape = calleeShape
                )
            }
        }
        if (calleeVisible) {
            Box(
                modifier = Modifier
                    .padding(calleePadding)
                    .fillMaxSize(callerAspectRatio)
                    .clip(calleeShape)
                    .background(calleeBackgroundColor, calleeShape)
                    .align(calleeAlignment)
            ) {
                VideoRenderer(
                    modifier = Modifier.fillMaxSize(),
                    videoTrack = localVideoTrack,
                    eglBaseContext = rtcManager?.eglBaseContext,
                    shape = calleeShape
                )
            }
        }
        if (callControlsVisible) {
            VideoCallControls(
                modifier = Modifier
                    .padding(controlsPadding)
                    .wrapContentSize()
                    .clip(controlsShape)
                    .background(controlsBackgroundColor, controlsShape)
                    .align(controlsAlignment),
                webRtcManager = rtcManager,
            )
        }
        if ((callerDevice != null || calleeDevice != null) && !isAccepted) {
            if (autoAnswerCall.not()) {
                IncomingCallScreen(
                    modifier = Modifier.fillMaxSize(),
                    caller = callerDevice,
                    callee = calleeDevice,
                    onAccept = {
                        isAccepted = true
                        rtcManager?.sendCallAccepted()
                        rtcManager?.unmute()
                    },
                    onDeny = {
                        rtcManager?.dismissCall(true)
                    }
                )
            }
        }
    }
    DisposableEffect(rtcManager) {
        rtcManager?.apply {
            initialize()
            mute()
        }
        onDispose {
            rtcManager?.release(CallEndReason.LOCAL_END)
        }
    }
}

@Composable
fun rememberRtcManager(
    callerIp: String?,
    context: Context = LocalContext.current,
    isDesign: Boolean = isDesignMode,
    onLocalTrackReady: CallManager.(VideoTrack) -> Unit = {},
    onRemoteTrackReady: CallManager.(VideoTrack) -> Unit = {},
    onAcceptCall: CallManager.() -> Unit = {},
    onCallEnded: CallManager.(CallEndReason) -> Unit = {},
    onCallStarted: CallManager.(SessionDescription) -> Unit = {}
) = remember(callerIp) {
    runCatching {
        if (isDesign) null
        else {
            CallManager(
                context = context,
                remoteIp = callerIp ?: "",
                isCaller = callerIp.isNullOrEmpty().not(),
                onLocalTrackReady = onLocalTrackReady,
                onRemoteTrackReady = onRemoteTrackReady,
                onAcceptCall = onAcceptCall,
                onCallStarted = onCallStarted,
                onCallEnded = onCallEnded
            )
        }
    }.onFailure { e ->
        e.printStackTrace()
    }.getOrNull()
}
