package org.mjdev.doorbellassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.mjdev.phone.ui.theme.base.PhoneCustomizer
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
            PhoneCustomizer {
                colorsLight.background = DarkMD5
                colorsDark.background = DarkMD5
            }
            content()
        }
    }
)
