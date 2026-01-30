/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

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
import androidx.compose.ui.graphics.vector.ImageVector

data class PhoneIcons (
    val videoCallRendererUser: ImageVector = Icons.Filled.AccountCircle,
    val itemCallIcon : ImageVector = Icons.Filled.Call,
    val userAccountIcon : ImageVector = Icons.Filled.AccountCircle,
    val buttonSettings : ImageVector = Icons.Filled.Settings,
    val buttonVolumeDown : ImageVector = Icons.AutoMirrored.Filled.VolumeDown,
    val buttonVolumeUp : ImageVector = Icons.AutoMirrored.Filled.VolumeUp,
    val buttonCallEnd : ImageVector =  Icons.Filled.CallEnd,
    val buttonCameraSwitch : ImageVector =  Icons.Filled.Cameraswitch,
    val buttonMicOn : ImageVector =  Icons.Filled.Mic,
    val buttonMicOff : ImageVector =  Icons.Filled.MicOff,
    val buttonVideocamOn: ImageVector =  Icons.Filled.Videocam,
    val buttonVideocamOff : ImageVector =  Icons.Filled.VideocamOff,
)
