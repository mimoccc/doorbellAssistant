package org.mjdev.phone.nsd.service

import org.mjdev.phone.nsd.device.NsdDevice

abstract class ServiceEvent {
    companion object {
        data class ServiceConnected(
            val address: String,
            val port: Int
        ) : ServiceEvent()

        data class ServiceNsdDevice(
            val device: NsdDevice
        ) : ServiceEvent()

        data class ServiceError(
            val error: Throwable
        ) : ServiceEvent()

        object NotYetImplemented : ServiceEvent()

        object ServiceDisconnected : ServiceEvent()
    }
}
