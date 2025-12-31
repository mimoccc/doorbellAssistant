package org.mjdev.doorbellassistant.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.stream.CallManager
import org.mjdev.doorbellassistant.ui.theme.Red
import org.mjdev.doorbellassistant.ui.theme.White

@Previews
@Composable
fun VideoCallControls(
    modifier: Modifier = Modifier,
    webRtcManager: CallManager? = null,
) {
    var isMuted by remember { mutableStateOf(false) }
    var isVideoEnabled by remember { mutableStateOf(true) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    Row(
        modifier = modifier, //.padding(horizontal = 16.dp),
    ) {
        IconButton(
            onClick = {
                isMuted = !isMuted
                webRtcManager?.toggleAudio(isMuted)
            }
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = if (isMuted) Red else White
            )
        }
        IconButton(
            onClick = {
                isVideoEnabled = !isVideoEnabled
                webRtcManager?.toggleVideo(isVideoEnabled)
            }
        ) {
            Icon(
                imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                contentDescription = if (isVideoEnabled) "Disable video" else "Enable video",
                tint = if (!isVideoEnabled) Red else White
            )
        }
        IconButton(onClick = { webRtcManager?.switchCamera() }) {
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Switch camera",
                tint = White
            )
        }
        IconButton(
            onClick = {
                isSpeakerOn = !isSpeakerOn
                webRtcManager?.toggleSpeaker(isSpeakerOn)
            }
        ) {
            Icon(
                imageVector = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp
                else Icons.AutoMirrored.Filled.VolumeDown,
                contentDescription = if (isSpeakerOn) "Speaker on" else "Speaker off",
                tint = White
            )
        }
        IconButton(
            onClick = {
                webRtcManager?.dismissCall(true)
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = Red)
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End call",
                tint = White
            )
        }
    }
}