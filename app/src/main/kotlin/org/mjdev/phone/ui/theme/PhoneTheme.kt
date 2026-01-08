package org.mjdev.phone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val darkColors = createColorScheme(
    // todo
)

val lightColors = createColorScheme(
    // todo
)

internal val LocalPhoneColorScheme = staticCompositionLocalOf { lightColors }

@Composable
fun PhoneTheme (
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColors
        else -> lightColors
    }
    PhoneMaterialTheme (
        phoneColorScheme = colorScheme,
        content = content
    )
}

@Composable
fun PhoneMaterialTheme(
    phoneColorScheme: PhoneColorScheme = darkColors,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPhoneColorScheme provides phoneColorScheme,
    ) {
        content()
    }
}

internal fun createColorScheme(
    background : Color = Color.White
): PhoneColorScheme = PhoneColorScheme(
    background = background
)

data class PhoneColorScheme (
    val background : Color
)
