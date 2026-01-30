/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.ui.theme.base

import org.mjdev.phone.ui.theme.DefaultDarkColors
import org.mjdev.phone.ui.theme.DefaultLightColors

data class PhoneColorScheme(
    var style: PhoneColorsStyle = PhoneColorsStyle.AUTO,
    var colorsLight: PhoneColors = DefaultLightColors,
    var colorsDark: PhoneColors = DefaultDarkColors,
    var paddings: PhonePaddings = PhonePaddings(),
    var alignments: PhoneAlignments = PhoneAlignments(),
    var shapes : PhoneShapes = PhoneShapes(),
    var icons : PhoneIcons = PhoneIcons(),
    var strings: PhoneStrings = PhoneStrings(),
    var assets: PhoneAssets = PhoneAssets(),
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