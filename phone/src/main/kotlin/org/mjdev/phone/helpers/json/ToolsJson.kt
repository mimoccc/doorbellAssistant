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

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object ToolsJson {
    val gson: Gson = GsonBuilder()
        .enableComplexMapKeySerialization()
        .serializeSpecialFloatingPointValues()
        .serializeNulls()
        .setPrettyPrinting()
        .registerTypeAdapterFactory(WrapperTypeAdapterFactory)
        .setExclusionStrategies(ExcludeDontSerializeStrategy)
        .create()

    inline fun <reified T> T.asJson(): String {
        val result = gson.toJson(this)
        return result
    }

    inline fun <reified T> String.fromJson(): T {
        val result = gson.fromJson(this, T::class.java) as T
        return result
    }
}
