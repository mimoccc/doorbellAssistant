package org.mjdev.phone.nsd.service

import org.mjdev.phone.nsd.device.NsdTypes

abstract class ServiceCommand {
    companion object {
        object GetNsdDevice : ServiceCommand()
        object GetState : ServiceCommand()

        data class GetNsdDevices(
            val types: List<NsdTypes>?
        ) : ServiceCommand()
    }
}
