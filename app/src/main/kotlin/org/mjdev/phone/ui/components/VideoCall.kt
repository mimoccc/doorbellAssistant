package org.mjdev.phone.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.stream.CallEndReason
import org.mjdev.phone.stream.CallManager
import org.mjdev.phone.ui.screens.IncomingCallScreen
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneAlignments
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phonePaddings
import org.mjdev.phone.ui.theme.base.phoneShapes
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack

@Previews
@Composable
fun VideoCall(
    modifier: Modifier = Modifier,
    callerDevice: NsdDevice? = null, // NsdDevice.EMPTY,
    calleeDevice: NsdDevice? = null, //NsdDevice.EMPTY,
    calleeVisible: Boolean = true,
    callerVisible: Boolean = true,
    callControlsVisible: Boolean = true,
    autoAnswerCall: Boolean = false,
    callerAspectRatio: Float = 0.4f,
    calleeAspectRatio: Float = 0.98f,
    onStartCall: (SessionDescription) -> Unit = {},
    onEndCall: (CallEndReason) -> Unit = {},
) = PhoneTheme {
    val context = LocalContext.current
    var localVideoTrack by remember { mutableStateOf<VideoTrack?>(null) }
    var remoteVideoTrack by remember { mutableStateOf<VideoTrack?>(null) }
    var isAccepted by remember { mutableStateOf(autoAnswerCall) }
    val rtcManager = rememberRtcManager(
        calleeIp = calleeDevice?.address,
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
                    .padding(phonePaddings.callerPadding)
                    .fillMaxSize(calleeAspectRatio)
                    .clip(phoneShapes.callerShape)
                    .background(phoneColors.callerBackgroundColor, phoneShapes.callerShape)
                    .align(phoneAlignments.callerAlignment)
            ) {
                VideoRenderer(
                    modifier = Modifier.fillMaxSize(),
                    videoTrack = remoteVideoTrack,
                    eglBaseContext = rtcManager?.eglBaseContext,
                )
            }
        }
        if (calleeVisible) {
            Box(
                modifier = Modifier
                    .padding(phonePaddings.calleePadding)
                    .fillMaxSize(callerAspectRatio)
                    .clip(phoneShapes.calleeShape)
                    .background(phoneColors.calleeBackgroundColor, phoneShapes.calleeShape)
                    .align(phoneAlignments.calleeAlignment)
            ) {
                VideoRenderer(
                    modifier = Modifier.fillMaxSize(),
                    videoTrack = localVideoTrack,
                    eglBaseContext = rtcManager?.eglBaseContext,
                )
            }
        }
        if (callControlsVisible) {
            VideoCallControls(
                modifier = Modifier
                    .padding(phonePaddings.controlsPadding)
                    .wrapContentSize()
                    .clip(phoneShapes.controlsShape)
                    .background(phoneColors.videoControlsBackground, phoneShapes.controlsShape)
                    .align(phoneAlignments.controlsAlignment),
                webRtcManager = rtcManager,
            )
        }
        if (!isAccepted) {
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
                        isAccepted = false
                        rtcManager?.unmute()
                        rtcManager?.dismissCall(true)
                    }
                )
            }
        }
    }
    DisposableEffect(rtcManager) {
        rtcManager?.apply {
            mute()
            initialize()
        }
        onDispose {
            rtcManager?.release(CallEndReason.LOCAL_END)
        }
    }
}

@Composable
fun rememberRtcManager(
    calleeIp: String?,
    context: Context = LocalContext.current,
    isDesign: Boolean = isPreview,
    onLocalTrackReady: CallManager.(VideoTrack) -> Unit = {},
    onRemoteTrackReady: CallManager.(VideoTrack) -> Unit = {},
    onAcceptCall: CallManager.() -> Unit = {},
    onCallEnded: CallManager.(CallEndReason) -> Unit = {},
    onCallStarted: CallManager.(SessionDescription) -> Unit = {}
) = remember(calleeIp) {
    runCatching {
        if (isDesign) null
        else {
            CallManager(
                context = context,
                remoteIp = calleeIp ?: "",
                isCaller = calleeIp.isNullOrEmpty().not(),
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
