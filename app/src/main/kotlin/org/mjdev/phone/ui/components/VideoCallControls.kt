package org.mjdev.phone.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.stream.CallManager
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phoneIcons

@Previews
@Composable
fun VideoCallControls(
    modifier: Modifier = Modifier,
    webRtcManager: CallManager? = null,
) = PhoneTheme(
    content = {
        var isMuted by remember { mutableStateOf(false) }
        var isVideoEnabled by remember { mutableStateOf(true) }
        var isSpeakerOn by remember { mutableStateOf(false) }
        Row(
            modifier = modifier,
        ) {
            IconButton(
                onClick = {
                    isMuted = !isMuted
                    webRtcManager?.toggleAudio(isMuted)
                }
            ) {
                Icon(
                    painter = if (isMuted) phoneIcons.buttonMicOff
                    else phoneIcons.buttonMicOn,
                    contentDescription = "",
                    tint = if (isMuted) phoneColors.controlsButtonDisabledIcon
                    else phoneColors.controlsButtonEnabledIcon
                )
            }
            IconButton(
                onClick = {
                    isVideoEnabled = !isVideoEnabled
                    webRtcManager?.toggleVideo(isVideoEnabled)
                }
            ) {
                Icon(
                    painter = if (isVideoEnabled) phoneIcons.buttonVideocamOn
                    else phoneIcons.buttonVideocamOff,
                    contentDescription = "",
                    tint = if (isVideoEnabled) phoneColors.controlsButtonEnabledIcon
                    else phoneColors.controlsButtonDisabledIcon
                )
            }
            IconButton(onClick = { webRtcManager?.switchCamera() }) {
                Icon(
                    painter = phoneIcons.buttonCameraSwitch,
                    contentDescription = "",
                    tint = phoneColors.controlsButtonEnabledIcon
                )
            }
            IconButton(
                onClick = {
                    isSpeakerOn = !isSpeakerOn
                    webRtcManager?.toggleSpeaker(isSpeakerOn)
                }
            ) {
                Icon(
                    painter = if (isSpeakerOn) phoneIcons.buttonVolumeUp
                    else phoneIcons.buttonVolumeDown,
                    contentDescription = "",
                    tint = phoneColors.controlsButtonEnabledIcon
                )
            }
            IconButton(
                onClick = {
                    webRtcManager?.dismissCall(true)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = phoneColors.controlButtonCallBackground
                )
            ) {
                Icon(
                    painter = phoneIcons.buttonCallEnd,
                    contentDescription = "",
                    tint = phoneColors.controlsButtonCallIcon
                )
            }
        }
    },
)