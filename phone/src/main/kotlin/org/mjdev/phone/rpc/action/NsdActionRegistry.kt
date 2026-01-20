/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.rpc.action

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import org.mjdev.phone.nsd.device.NsdDevice
import kotlin.reflect.KClass

class NsdActionRegistry {
    private val actions = mutableListOf<KClass<out NsdAction>>()

    @OptIn(InternalSerializationApi::class)
    private val nsdSerializersModule
        get() = SerializersModule {
            contextual(NsdDevice::class, NsdDevice.serializer())
            polymorphic(NsdAction::class) {
                getAll().forEach { klass ->
                    @Suppress("UNCHECKED_CAST")
                    subclass(
                        klass as KClass<NsdAction>,
                        klass.serializer()
                    )
                }
            }
        }

    @OptIn(ExperimentalSerializationApi::class)
    val json
        get() = Json {
            serializersModule = nsdSerializersModule
            classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
            prettyPrint = true
            isLenient = true
            encodeDefaults = true
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }

    fun register(
        cls: KClass<out NsdAction>
    ) {
        actions.add(cls)
    }

    fun getAll(): List<KClass<out NsdAction>> = actions.toList().distinct()
}