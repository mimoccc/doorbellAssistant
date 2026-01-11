@file:Suppress("UnusedImport")

package org.mjdev.phone.ui.theme.base

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.input.key.Key.Companion.VolumeDown
import org.mjdev.phone.extensions.CustomExtensions.provideDelegate

class PhoneIcons {
    val videoCallRendererUser: Painter by Icons.Filled.AccountCircle
    val itemCallIcon : Painter by Icons.Filled.Call
    val userAccountIcon : Painter by Icons.Filled.AccountCircle
    val buttonSettings : Painter by Icons.Filled.Settings
    val buttonVolumeDown : Painter by Icons.AutoMirrored.Filled.VolumeDown
    val buttonVolumeUp : Painter by Icons.AutoMirrored.Filled.VolumeUp
    val buttonCallEnd : Painter by  Icons.Filled.CallEnd
    val buttonCameraSwitch : Painter by  Icons.Filled.Cameraswitch
    val buttonMicOn : Painter by  Icons.Filled.Mic
    val buttonMicOff : Painter by  Icons.Filled.MicOff
    val buttonVideocamOn: Painter by  Icons.Filled.Videocam
    val buttonVideocamOff : Painter by  Icons.Filled.VideocamOff
}

