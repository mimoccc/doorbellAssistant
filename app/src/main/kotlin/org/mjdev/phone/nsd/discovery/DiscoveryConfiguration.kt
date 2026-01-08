package org.mjdev.phone.nsd.discovery

import android.net.nsd.NsdManager
import org.mjdev.phone.nsd.manager.ProtocolType

data class DiscoveryConfiguration(
    val type: String,
    @Suppress("ANNOTATION_WILL_BE_APPLIED_ALSO_TO_PROPERTY_OR_FIELD")
    @ProtocolType
    val protocolType: Int = NsdManager.PROTOCOL_DNS_SD
)
