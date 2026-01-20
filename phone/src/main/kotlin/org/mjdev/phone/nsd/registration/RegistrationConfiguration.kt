package org.mjdev.phone.nsd.registration

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import org.mjdev.phone.nsd.manager.ProtocolType

data class RegistrationConfiguration(
    val nsdServiceInfo: NsdServiceInfo,
    @Suppress("ANNOTATION_WILL_BE_APPLIED_ALSO_TO_PROPERTY_OR_FIELD")
    @ProtocolType
    val protocolType: Int = NsdManager.PROTOCOL_DNS_SD,
) {
    val serviceName: String?
        get() = nsdServiceInfo.serviceName

    val serviceType: String?
        get() = nsdServiceInfo.serviceType

    val port
        get() = nsdServiceInfo.port
}
