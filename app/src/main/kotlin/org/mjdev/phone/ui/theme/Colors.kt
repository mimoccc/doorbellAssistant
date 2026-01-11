package org.mjdev.phone.ui.theme

import androidx.compose.ui.graphics.Color
import org.mjdev.phone.ui.theme.base.PhoneColors

val DefaultLightColors = PhoneColors(
    previewBackground = Color.White,
    background = Color.White,
    textColor = Color.Black,
    iconsBackground = Color.DarkGray,
    labelsBackground = Color.Gray,
    callerIconBorderColor = Color.LightGray,
    iconTint = Color.LightGray,
    labelTextColor = Color.Black,
    videoControlsBackground = Color.DarkGray,
    callerBackgroundColor = Color.DarkGray.copy(0.3f),
    calleeBackgroundColor = Color.DarkGray.copy(0.3f),
    videoCallRendererUser = Color.DarkGray,
    controlsButtonEnabledIcon = Color.White,
    controlsButtonDisabledIcon = Color.DarkGray,
    controlButtonCallBackground = Color.Red,
    controlsButtonCallIcon = Color.White,
    glowColor = Color.Black,
)

val DefaultDarkColors = PhoneColors(
    previewBackground = Color.Black,
    background = Color.DarkGray,
    textColor = Color.Black,
    iconsBackground = Color.White,
    labelsBackground = Color.Gray,
    callerIconBorderColor = Color.Gray,
    iconTint = Color.DarkGray,
    labelTextColor = Color.LightGray,
    videoControlsBackground = Color.DarkGray,
    callerBackgroundColor = Color.White.copy(0.3f),
    calleeBackgroundColor = Color.White.copy(0.3f),
    videoCallRendererUser = Color.LightGray,
    controlsButtonEnabledIcon = Color.White,
    controlsButtonDisabledIcon = Color.DarkGray,
    controlButtonCallBackground = Color.Red,
    controlsButtonCallIcon = Color.White,
    glowColor = Color.White,
)
