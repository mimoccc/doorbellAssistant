package org.mjdev.phone.ui.theme.base

import org.mjdev.phone.ui.theme.DefaultDarkColors
import org.mjdev.phone.ui.theme.DefaultLightColors

data class PhoneColorScheme(
    var style: PhoneColorsStyle = PhoneColorsStyle.AUTO,
    var colorsLight: PhoneColors = DefaultLightColors,
    var colorsDark: PhoneColors = DefaultDarkColors,
    // todo
    var paddings: PhonePaddings = PhonePaddings(),
    // todo
    var alignments: PhoneAlignments = PhoneAlignments(),
    // todo
    var shapes : PhoneShapes = PhoneShapes(),
    // todo
    var icons : PhoneIcons = PhoneIcons(),
    // todo
    var strings: PhoneStrings = PhoneStrings()
) {
    fun getColors(
        isDarkTheme: Boolean
    ): PhoneColors = if (isDarkTheme) colorsDark else colorsLight

    companion object {
        val EmptyColorScheme = PhoneColorScheme (
            style = PhoneColorsStyle.EMPTY
        )
    }
}