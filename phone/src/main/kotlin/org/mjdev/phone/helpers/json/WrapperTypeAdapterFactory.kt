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
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

object WrapperTypeAdapterFactory : TypeAdapterFactory {
    const val META_SERIALIZED_CLASS = "serialized_class"

    override fun <T> create(
        gson: Gson,
        type: TypeToken<T>
    ): TypeAdapter<T>? {
        val delegate = gson.getDelegateAdapter(this, type)
        val elementAdapter = gson.getAdapter(JsonElement::class.java)
        val rawType = type.rawType
        val isSerializable = rawType.isAnnotationPresent(Serializable::class.java)
        if (!isSerializable) return delegate
        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) {
                runCatching {
                    if (value == null) {
                        elementAdapter.write(out, JsonNull.INSTANCE)
                    } else {
                        val payload = delegate.toJsonTree(value).asJsonObject
                        payload.addProperty(META_SERIALIZED_CLASS, value::class.java.name)
                        elementAdapter.write(out, payload)
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun read(reader: JsonReader): T? = runCatching {
                val element = elementAdapter.read(reader) ?: return null
                if (!element.isJsonObject) return delegate.fromJsonTree(element)
                val obj = element.asJsonObject
                val typeName = obj.remove(META_SERIALIZED_CLASS)?.asString
                val clazz = if (typeName != null) Class.forName(typeName) else rawType
                val specificToken = TypeToken.get(clazz)
                val specificDelegate =
                    gson.getDelegateAdapter(this@WrapperTypeAdapterFactory, specificToken)
                val result = specificDelegate.fromJsonTree(obj) as? T
                return result
            }.onFailure { e ->
                e.printStackTrace()
            }.getOrNull()
        }
    }
}
