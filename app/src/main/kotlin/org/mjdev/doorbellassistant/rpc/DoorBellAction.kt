/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

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
