/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.nsd.device

import org.mjdev.phone.helpers.json.Serializable
import kotlin.reflect.KProperty

// todo values to string and vice versa
@Serializable
abstract class INsdDetail(
    val map: MutableMap<String, String> = HashMap<String, String>()
) {
    operator fun get(key: String) :String = map[key] ?: ""
    operator fun set(key: String, value: String) = map.set(key, value)

    inline fun  forEach(
        action: (Map.Entry<String, String>) -> Unit
    ) = map.forEach { element -> action(element) }

    inline fun <R> mapAs(
        transform: (Map.Entry<String, String>) -> R
    ): List<R> = map.map(transform)

    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): String = map.getOrDefault(property.name, "")

    operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>, value: String
    ) {
        map[property.name] = value
    }
}
