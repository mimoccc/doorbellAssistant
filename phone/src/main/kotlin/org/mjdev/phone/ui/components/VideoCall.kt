/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import org.mjdev.phone.extensions.CustomExt.isPreview
import org.mjdev.phone.extensions.CustomExt.postDelayed
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdDevice.Companion.isAutoAnswerCall
import org.mjdev.phone.nsd.service.CallNsdService.Companion.rememberCallNsdService
import org.mjdev.phone.rpc.server.INsdServerRPC
import org.mjdev.phone.stream.CallEndReason
import org.mjdev.phone.stream.CallManager
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneAlignments
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phonePaddings
import org.mjdev.phone.ui.theme.base.phoneShapes
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack

@Suppress("ParamsComparedByRef")
@Previews
@Composable
fun VideoCall(
    modifier: Modifier = Modifier,
    caller: NsdDevice = NsdDevice.EMPTY,
    callee: NsdDevice = NsdDevice.EMPTY,
    ringtone: String = "ringtone/ringtone.ogg",
    isCaller: Boolean = false,
    calleeVisible: Boolean = true,
    callerVisible: Boolean = true,
    callControlsVisible: Boolean = true,
    callerAspectRatio: Float = 0.4f,
    calleeAspectRatio: Float = 0.98f,
    onStartCall: (SessionDescription) -> Unit = {},
    onEndCall: (CallEndReason) -> Unit = {},
) = PhoneTheme {
    val isAutoAnswerCall by remember(caller) {
        derivedStateOf {
            isPreview || (callee.isAutoAnswerCall && !isCaller)
        }
    }
    Box(
        modifier = modifier
    ) {
        val callService by rememberCallNsdService().collectAsState(null)
        var localVideoTrack by remember { mutableStateOf<VideoTrack?>(null) }
        var remoteVideoTrack by remember { mutableStateOf<VideoTrack?>(null) }
        var isAccepted by remember(caller, callee) {
            mutableStateOf(isAutoAnswerCall)
        }
        val callRpcServer by remember(callService) {
            derivedStateOf {
                callService?.rpcServer
            }
        }
        // todo : better performance pls, remove!
        if (callRpcServer != null) {
            val callManager = rememberCallManager(
                callee = callee,
                caller = caller,
                nsdRpcServer = callRpcServer,
                isCaller = isCaller,
                onLocalTrackReady = { track ->
                    localVideoTrack = track
                },
                onRemoteTrackReady = { track ->
                    remoteVideoTrack = track
                },
                onAcceptCall = {
                    isAccepted = true
                },
                onCallStarted = { sdp ->
                    onStartCall(sdp)
                },
                onCallEnded = { reason ->
                    remoteVideoTrack = null
                    onEndCall(reason)
                },
            )
            if (callerVisible) {
                Box(
                    modifier = Modifier
                        .padding(phonePaddings.callerPadding)
                        .fillMaxSize(calleeAspectRatio)
                        .clip(phoneShapes.callerShape)
                        .background(phoneColors.colorCallerBackground, phoneShapes.callerShape)
                        .align(phoneAlignments.callerAlignment)
                ) {
                    VideoRenderer(
                        modifier = Modifier.fillMaxSize(),
                        videoTrack = remoteVideoTrack,
                        eglBaseContext = callManager?.eglBaseContext,
                    )
                }
            }
            if (calleeVisible) {
                Box(
                    modifier = Modifier
                        .padding(phonePaddings.calleePadding)
                        .fillMaxSize(callerAspectRatio)
                        .clip(phoneShapes.calleeShape)
                        .background(phoneColors.colorCalleeBackground, phoneShapes.calleeShape)
                        .align(phoneAlignments.calleeAlignment)
                ) {
                    VideoRenderer(
                        modifier = Modifier.fillMaxSize(),
                        videoTrack = localVideoTrack,
                        eglBaseContext = callManager?.eglBaseContext,
                    )
                }
            }
            if (callControlsVisible) {
                VideoCallControls(
                    modifier = Modifier
                        .padding(phonePaddings.controlsPadding)
                        .wrapContentSize()
                        .clip(phoneShapes.controlsShape)
                        .background(
                            phoneColors.colorVideoControlsBackground,
                            phoneShapes.controlsShape
                        )
                        .align(phoneAlignments.controlsAlignment),
                    webRtcManager = callManager,
                )
            }
            if (!isAccepted) {
                callManager?.mute()
                CallScreen(
                    modifier = Modifier.fillMaxSize(),
                    caller = caller,
                    callee = callee,
                    ringtone = ringtone,
                    onAccept = {
                        isAccepted = true
                        callManager?.unmute()
                        callManager?.callAccept()
                    },
                    onDeny = {
                        isAccepted = false
                        callManager?.dismissCall(true)
                        postDelayed(500L) {
                            callManager?.unmute()
                        }
                    }
                )
            } else {
                callManager?.unmute()
                callManager?.callAccept()
            }
            DisposableEffect(callManager) {
                callManager?.apply {
                    initialize()
                    mute()
                    if (isCaller) {
                        callRpcServer?.sendCallStart(caller, callee)
                    } else {
                        callRpcServer?.sendCallStarted(caller, callee)
                    }
                }
                onDispose {
                    callManager?.release()
                }
            }
        }
    }
}

@Suppress("ParamsComparedByRef")
@Composable
fun rememberCallManager(
    caller: NsdDevice,
    callee: NsdDevice,
    nsdRpcServer: INsdServerRPC?,
    context: Context = LocalContext.current,
    isCaller: Boolean = false,
    isDesign: Boolean = isPreview,
    onLocalTrackReady: CallManager.(VideoTrack) -> Unit = {},
    onRemoteTrackReady: CallManager.(VideoTrack) -> Unit = {},
    onAcceptCall: CallManager.(String) -> Unit = {},
    onCallEnded: CallManager.(CallEndReason) -> Unit = {},
    onCallStarted: CallManager.(SessionDescription) -> Unit = {}
) = remember(callee, nsdRpcServer) {
    runCatching {
        if (isDesign) null else {
            CallManager(
                context = context,
                caller = caller,
                callee = callee,
                nsdRpcServer = nsdRpcServer,
                isCaller = isCaller,
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
