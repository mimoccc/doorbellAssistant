package org.mjdev.phone.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
) = PhoneTheme {
    var isMuted by remember { mutableStateOf(false) }
    var isVideoEnabled by remember { mutableStateOf(true) }
    var isSpeakerOn by remember { mutableStateOf(true) }  // Speaker is ON by default in CallManager
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(26.dp)
                .clip(CircleShape),
            onClick = {
                isMuted = !isMuted
                webRtcManager?.toggleAudio(isMuted)
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = if (isMuted) phoneIcons.buttonMicOff
                else phoneIcons.buttonMicOn,
                contentDescription = "",
                tint = if (isMuted) phoneColors.colorControlsButtonDisabledIcon
                else phoneColors.colorControlsButtonEnabledIcon
            )
        }
        IconButton(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(26.dp)
                .clip(CircleShape),
            onClick = {
                isVideoEnabled = !isVideoEnabled
                webRtcManager?.toggleVideo(isVideoEnabled)
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = if (isVideoEnabled) phoneIcons.buttonVideocamOn
                else phoneIcons.buttonVideocamOff,
                contentDescription = "",
                tint = if (isVideoEnabled) phoneColors.colorControlsButtonEnabledIcon
                else phoneColors.colorControlsButtonDisabledIcon
            )
        }
        IconButton(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(26.dp)
                .clip(CircleShape),
            onClick = {
                webRtcManager?.switchCamera()
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = phoneIcons.buttonCameraSwitch,
                contentDescription = "",
                tint = phoneColors.colorControlsButtonEnabledIcon
            )
        }
        IconButton(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(26.dp)
                .clip(CircleShape),
            onClick = {
                isSpeakerOn = !isSpeakerOn
                webRtcManager?.toggleSpeaker(isSpeakerOn)
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = if (isSpeakerOn) phoneIcons.buttonVolumeUp
                else phoneIcons.buttonVolumeDown,
                contentDescription = "",
                tint = phoneColors.colorControlsButtonEnabledIcon
            )
        }
        IconButton(
            modifier = Modifier
                .padding(start = 8.dp, end = 0.dp)
                .size(38.dp)
                .clip(CircleShape),
            onClick = {
                webRtcManager?.dismissCall(true)
            },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = phoneColors.colorControlButtonCallBackground
            ),
        ) {
            Icon(
                modifier = Modifier
                    .size(38.dp)
                    .padding(4.dp),
                painter = phoneIcons.buttonCallEnd,
                contentDescription = "",
                tint = phoneColors.colorControlsButtonCallIcon
            )
        }
    }
}
