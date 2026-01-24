/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

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
    colorButtonColor = Color.DarkGray,
    colorScrim = Color.White.copy(alpha = 0.3f)
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
    colorButtonColor = Color.LightGray,
    colorScrim = Color.Black.copy(alpha = 0.3f)
)
