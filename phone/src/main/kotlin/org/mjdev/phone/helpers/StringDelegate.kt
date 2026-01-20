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

import kotlin.reflect.KProperty

class StringDelegate(
    private val str: String
) {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): String = str

    companion object {
        operator fun String.provideDelegate(
            thisRef: Any?,
            property: KProperty<*>
        ): StringDelegate = StringDelegate(this)
    }
}