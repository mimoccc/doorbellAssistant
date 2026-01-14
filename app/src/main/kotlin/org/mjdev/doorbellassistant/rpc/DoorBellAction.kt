package org.mjdev.doorbellassistant.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.rpc.action.NsdAction

object DoorBellActions {

    @Serializable
    @SerialName("DoorBellActionMotionDetected")
    class DoorBellActionMotionDetected(
        val device: NsdDevice?
    ) : NsdAction()

    @Serializable
    @SerialName("DoorBellActionMotionUnDetected")
    class DoorBellActionMotionUnDetected(
        val device: NsdDevice?
    ) : NsdAction()

}
