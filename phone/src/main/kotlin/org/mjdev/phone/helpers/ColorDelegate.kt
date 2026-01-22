/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.helpers

import androidx.compose.ui.graphics.Color
import kotlin.reflect.KProperty

class ColorDelegate(
    private var color: Color
) {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): Color = color

    operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>, value: Color
    ) {
        color = value
    }

    companion object {
        operator fun Color.provideDelegate(
            thisRef: Any?,
            property: KProperty<*>
        ): ColorDelegate = ColorDelegate(this)
    }
}