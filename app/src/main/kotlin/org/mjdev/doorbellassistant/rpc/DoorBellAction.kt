package org.mjdev.doorbellassistant.rpc

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.mjdev.doorbellassistant.nsd.device.NsdDevice

@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
@Serializable
sealed class DoorBellAction {

    @Serializable
    @SerialName("DoorBellActionMotionDetected")
    class DoorBellActionMotionDetected(
        val device: NsdDevice?
    ) : DoorBellAction()

    @Serializable
    @SerialName("DoorBellActionMotionUnDetected")
    class DoorBellActionMotionUnDetected(
        val device: NsdDevice?
    ) : DoorBellAction()

    @Serializable
    @SerialName("DoorBellActionCall")
    class DoorBellActionCall(
        val caller: NsdDevice?,
        val callee: NsdDevice?
    ) : DoorBellAction()

}
