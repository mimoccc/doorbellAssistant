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

import org.mjdev.phone.helpers.json.Serializable
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.rpc.action.NsdAction

@Serializable
open class DoorBellActions {

    @Serializable
    data class DoorBellActionMotionDetected(
        val device: NsdDevice?
    ) : NsdAction()

    @Serializable
    data class DoorBellActionMotionUnDetected(
        val device: NsdDevice?
    ) : NsdAction()

}
