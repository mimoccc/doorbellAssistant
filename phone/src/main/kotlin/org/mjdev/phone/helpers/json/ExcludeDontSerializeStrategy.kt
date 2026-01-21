/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.helpers.json

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

object ExcludeDontSerializeStrategy : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes?): Boolean {
        return if (f?.getAnnotation(DontSerialize::class.java) != null) {
            true
        } else f?.declaredClass?.isProhibited ?: false
    }

    override fun shouldSkipClass(clazz: Class<*>?): Boolean {
        return if (clazz?.getAnnotation(DontSerialize::class.java) != null) {
            true
        } else clazz?.isProhibited ?: false
    }

    val Class<*>.isProhibited: Boolean
        get() = this.name.let { className ->
            className.startsWith("androidx.compose.ui.") ||
                    className.startsWith("androidx.compose.foundation.") ||
                    className.startsWith("androidx.compose.material.")
        }
}
