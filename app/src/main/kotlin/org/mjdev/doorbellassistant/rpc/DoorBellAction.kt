package org.mjdev.doorbellassistant.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdDevice

@Serializable
sealed class DoorBellAction {

    @Serializable
    @SerialName("DoorBellActionMotionDetected")
    class DoorBellActionMotionDetected(
        val device: NsdDevice
    ) : DoorBellAction()

    @Serializable
    @SerialName("DoorBellActionMotionUnDetected")
    class DoorBellActionMotionUnDetected(
        val device: NsdDevice
    ) : DoorBellAction()

}
