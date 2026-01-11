@file:Suppress("unused")

package org.mjdev.phone.ui.theme.base

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.ui.theme.DefaultDarkColors
import org.mjdev.phone.ui.theme.DefaultLightColors
import org.mjdev.phone.ui.theme.base.PhoneColorScheme.Companion.EmptyColorScheme

val DefaultPhoneColorScheme = PhoneColorScheme(
    style = PhoneColorsStyle.AUTO,
    colorsLight = DefaultLightColors,
    colorsDark = DefaultDarkColors
)

val customTheme = mutableStateOf(DefaultPhoneColorScheme)

val LocalPhoneColors = compositionLocalOf {
    EmptyColorScheme
}

internal val phoneColorScheme: PhoneColorScheme
    @Composable
    get() = LocalPhoneColors.current

val phoneColors: PhoneColors
    @Composable
    get() {
        val colorScheme = phoneColorScheme
        val isDarkTheme: Boolean = shouldBeDarkTheme(colorScheme)
        return colorScheme.getColors(isDarkTheme)
    }

val phonePaddings: PhonePaddings
    @Composable
    get() {
        val colorScheme = phoneColorScheme
        return colorScheme.paddings
    }

val phoneAlignments: PhoneAlignments
    @Composable
    get() {
        val colorScheme = phoneColorScheme
        return colorScheme.alignments
    }

val phoneShapes: PhoneShapes
    @Composable
    get() {
        val colorScheme = phoneColorScheme
        return colorScheme.shapes
    }

val phoneIcons: PhoneIcons
    @Composable
    get() {
        val colorScheme = phoneColorScheme
        return colorScheme.icons
    }

val phoneStrings: PhoneStrings
    @Composable
    get() {
        val colorScheme = phoneColorScheme
        return colorScheme.strings
    }

@Composable
fun shouldBeDarkTheme(
    colorScheme: PhoneColorScheme = customTheme.value,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
) = when (colorScheme.style) {
    PhoneColorsStyle.EMPTY -> false
    PhoneColorsStyle.DARK -> true
    PhoneColorsStyle.LIGHT -> false
    PhoneColorsStyle.AUTO -> isDarkTheme
}

@Composable
fun PreviewBackground(
    isPreviewMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (isPreviewMode) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(phoneColors.previewBackground)
        ) {
            content()
        }
    } else {
        content()
    }
}

@Composable
fun PhoneTheme(
    colorScheme: PhoneColorScheme = customTheme.value,
    style: PhoneColorsStyle = PhoneColorsStyle.AUTO,
    showBackground: Boolean = isPreview,
    content: @Composable PhoneColorScheme.() -> Unit,
) {
    val current = LocalPhoneColors.current
    val actualTheme = if (current === EmptyColorScheme) colorScheme.copy(style = style) else current
    CompositionLocalProvider(LocalPhoneColors provides actualTheme) {
        PreviewBackground(showBackground) {
            content(colorScheme)
        }
    }
}

@Composable
fun PhoneThemeLight(
    colorScheme: PhoneColorScheme = customTheme.value,
    content: @Composable PhoneColorScheme.() -> Unit
) = PhoneTheme(colorScheme, PhoneColorsStyle.LIGHT, content = content)

@Composable
fun PhoneThemeDark(
    colorScheme: PhoneColorScheme = customTheme.value,
    content: @Composable PhoneColorScheme.() -> Unit
) = PhoneTheme(colorScheme, PhoneColorsStyle.DARK, content =content)

fun phoneCustomizer (
    customizer : PhoneColorScheme.() -> Unit
) {
    customTheme.value = DefaultPhoneColorScheme.apply(customizer)
}

@Composable
fun PhoneCustomizer(
    customizer: @Composable PhoneColorScheme.() -> Unit
) {
    customTheme.value = DefaultPhoneColorScheme.copy().apply { customizer() }
}
