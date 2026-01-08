package org.mjdev.phone.helpers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json

object  ToolsJson {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
        prettyPrint = true
        isLenient = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
//        explicitNulls = true
//        coerceInputValues = true
//        decodeEnumsCaseInsensitive = true
//        allowStructuredMapKeys = true
    }

    inline fun <reified T : Any> T.asJson(): String {
        return json.encodeToString<T>(this)
    }

    inline fun <reified T> String.fromJson(): T = runCatching {
        return json.decodeFromString<T>(this)
    }.getOrNull() as T

}
