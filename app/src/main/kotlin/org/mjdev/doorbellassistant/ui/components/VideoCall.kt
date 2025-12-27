package org.mjdev.doorbellassistant.ui.components

import android.content.Context
import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.video.android.compose.permission.LaunchCallPermissions
import io.getstream.video.android.compose.permission.VideoPermissionsState
import io.getstream.video.android.compose.permission.rememberCallPermissionsState
import io.getstream.video.android.compose.theme.StreamColors
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.CallAppBar
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
import io.getstream.video.android.compose.ui.components.call.renderer.RegularVideoRendererStyle
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.core.call.state.AcceptCall
import io.getstream.video.android.core.call.state.CancelCall
import io.getstream.video.android.core.call.state.ChatDialog
import io.getstream.video.android.core.call.state.ChooseLayout
import io.getstream.video.android.core.call.state.ClosedCaptionsAction
import io.getstream.video.android.core.call.state.DeclineCall
import io.getstream.video.android.core.call.state.FlipCamera
import io.getstream.video.android.core.call.state.InviteUsersToCall
import io.getstream.video.android.core.call.state.LeaveCall
import io.getstream.video.android.core.call.state.Reaction
import io.getstream.video.android.core.call.state.SelectAudioDevice
import io.getstream.video.android.core.call.state.Settings
import io.getstream.video.android.core.call.state.ShowCallParticipantInfo
import io.getstream.video.android.core.call.state.ToggleCamera
import io.getstream.video.android.core.call.state.ToggleHifiAudio
import io.getstream.video.android.core.call.state.ToggleMicrophone
import io.getstream.video.android.core.call.state.ToggleScreenConfiguration
import io.getstream.video.android.core.call.state.ToggleSpeakerphone
import io.getstream.video.android.mock.StreamPreviewDataUtils
import io.getstream.video.android.mock.previewCall
import io.getstream.video.android.model.User
import org.mjdev.doorbellassistant.BuildConfig
import org.mjdev.doorbellassistant.activity.AssistantActivity.Companion.isDoorBellAssistantRunning
import org.mjdev.doorbellassistant.extensions.ComposeExt.currentWifiSSID
import org.mjdev.doorbellassistant.extensions.ComposeExt.isDesignMode
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdTypes
import org.mjdev.doorbellassistant.ui.theme.DarkMD5

@Suppress("DEPRECATION")
@Preview
@Composable
fun VideoCall(
    modifier: Modifier = Modifier,
    device: NsdDevice? = null,
    applicationContext: Context = LocalContext.current.applicationContext,
    callId: String = applicationContext.currentWifiSSID,
    userId: String = device?.serviceName ?: "user-id",
    userName: String = device?.serviceName ?: "user-name",
    userImage: String? = null, // uri?
    userRole: String = "admin",
    onStart: () -> Unit = {},
    onDismiss: () -> Unit = {},
    isDesignModeEnabled: Boolean = isDesignMode
) {
    val isAssistant = remember(device) {
        applicationContext.isDoorBellAssistantRunning
    }
    val call = remember(device) {
        if (isDesignModeEnabled) {
            StreamPreviewDataUtils.initializeStreamVideo(applicationContext)
            previewCall
        } else {
            StreamVideoBuilder(
                context = applicationContext,
                apiKey = BuildConfig.STREAM_API_KEY,
                token = BuildConfig.STREAM_API_USER_TOKEN,
                geo = GEO.GlobalEdgeNetwork,
                user = User(
                    id = userId,
                    name = userName,
                    image = userImage,
                    role = userRole,
                ),
            ).build().call(
                type = "default",
                id = callId
            )
        }
    }
    val permissions: VideoPermissionsState = rememberCallPermissionsState(call = call)
    val style = RegularVideoRendererStyle().copy(
        isScreenSharing = true,
        isShowingConnectionQualityIndicator = true,
        labelPosition = Alignment.BottomStart,
        reactionPosition = Alignment.BottomCenter
    )
    val appBarContent: @Composable (call: Call) -> Unit = {
        if (isAssistant.not()) CallAppBar(
            call = call,
            onBackPressed = onDismiss,
            onCallAction = { callAction ->
                when (callAction) {
                    is FlipCamera -> {
                        if (!applicationContext.isDoorBellAssistantRunning) {
                            call.camera.flip()
                        }
                    }

                    is ToggleCamera -> {
                        if (!applicationContext.isDoorBellAssistantRunning) {
                            call.camera.setEnabled(callAction.isEnabled)
                        }
                    }

                    is ToggleMicrophone -> {
                        if (!applicationContext.isDoorBellAssistantRunning) {
                            call.microphone.setEnabled(callAction.isEnabled)
                        }
                    }

                    is ToggleSpeakerphone -> {
                    }

                    is SelectAudioDevice -> {
                    }

                    is ToggleHifiAudio -> {
                    }

                    is AcceptCall -> {
                    }

                    is CancelCall -> {
                    }

                    is DeclineCall -> {
                    }

                    is ChatDialog -> {
                    }

                    is Settings -> {
                    }

                    is ClosedCaptionsAction -> {
                    }

                    is Reaction -> {
                    }

                    is ChooseLayout -> {
                    }

                    is InviteUsersToCall -> {
                    }

                    is ToggleScreenConfiguration -> {
                    }

                    is ShowCallParticipantInfo -> {
                    }

                    is LeaveCall -> {
                        if (!applicationContext.isDoorBellAssistantRunning) {
                            onDismiss()
                        }
                    }

                    else -> Unit
                }
            },
        )
    }
    VideoTheme(
        colors = StreamColors.defaultColors().copy(
            brandPrimary = DarkMD5,
            brandPrimaryLt = DarkMD5,
            brandRedDk = DarkMD5,
            baseSenary = DarkMD5,
        ),
    ) {
        CallContent(
            modifier = modifier,
            call = call,
            permissions = permissions,
            enableInPictureInPicture = true,
            onBackPressed = { onDismiss() },
            style = style,
            appBarContent = {
                appBarContent(call)
            },
            enableDiagnostics = true,
            videoOverlayContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            bottom = 32.dp,
                            start = 8.dp,
                            end = 8.dp,
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    NsdItem(
                        device = device,
                    )
                }
            },
        )
    }
    LaunchCallPermissions(call = call) {
        runCatching {
            call.join(create = true)
        }
    }
    DisposableEffect(device) {
        onStart()
        onDispose {
            runCatching {
                StreamVideo.removeClient()
            }
        }
    }
}