/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.service

import org.mjdev.phone.helpers.json.Serializable
import org.mjdev.phone.nsd.device.NsdType

@Serializable
open class ServiceCommand {
    @Serializable
    object GetNsdDevice : ServiceCommand()

    @Serializable
    object GetState : ServiceCommand()

    @Serializable
    data class GetNsdDevices(
        val types: List<NsdType>?
    ) : ServiceCommand()
}