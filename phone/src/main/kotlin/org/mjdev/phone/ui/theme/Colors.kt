package org.mjdev.phone.ui.theme

import androidx.compose.ui.graphics.Color
import org.mjdev.phone.ui.theme.base.PhoneColors

val DefaultLightColors = PhoneColors(
    colorPreviewBackground = Color.White,
    colorBackground = Color.White,
    colorText = Color.Black,
    colorIconsBackground = Color.DarkGray,
    colorLabelsBackground = Color.Gray,
    colorCallerIconBorder = Color.LightGray,
    colorIconTint = Color.LightGray,
    colorLabelText = Color.Black,
    colorVideoControlsBackground = Color.DarkGray,
    colorCallerBackground = Color.DarkGray.copy(0.3f),
    colorCalleeBackground = Color.DarkGray.copy(0.3f),
    colorVideoCallRendererUser = Color.DarkGray,
    colorControlsButtonEnabledIcon = Color.Black,
    colorControlsButtonDisabledIcon = Color.DarkGray,
    colorControlButtonCallBackground = Color.Red,
    colorControlsButtonCallIcon = Color.White,
    colorGlow = Color.Black,
    colorCallScreenText = Color.DarkGray,
)

val DefaultDarkColors = PhoneColors(
    colorPreviewBackground = Color.Black,
    colorBackground = Color.DarkGray,
    colorText = Color.Black,
    colorIconsBackground = Color.White,
    colorLabelsBackground = Color.Gray,
    colorCallerIconBorder = Color.Gray,
    colorIconTint = Color.DarkGray,
    colorLabelText = Color.LightGray,
    colorVideoControlsBackground = Color.DarkGray,
    colorCallerBackground = Color.White.copy(0.3f),
    colorCalleeBackground = Color.White.copy(0.3f),
    colorVideoCallRendererUser = Color.LightGray,
    colorControlsButtonEnabledIcon = Color.White,
    colorControlsButtonDisabledIcon = Color.DarkGray,
    colorControlButtonCallBackground = Color.Red,
    colorControlsButtonCallIcon = Color.White,
    colorGlow = Color.White,
    colorCallScreenText = Color.LightGray,
)
