/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.mjdev.phone.ui.theme.base.PhoneTheme

private val AppColorScheme = lightColorScheme(
    primary = LightMD5,
    secondary = DarkMD5,
    tertiary = White,
    background = DarkMD5,
    surface = Color(0xFFFFFBFE),
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Suppress("unused")
@Composable
fun DoorBellAssistantTheme(
    content: @Composable () -> Unit
) = MaterialTheme(
    colorScheme = AppColorScheme,
    typography = Typography,
    content = {
        PhoneTheme {
            content()
        }
    }
)
