package org.mjdev.doorbellassistant.helpers.nsd.discovery

import android.net.nsd.NsdManager
import org.mjdev.doorbellassistant.helpers.nsd.ProtocolType

data class DiscoveryConfiguration(
    val type: String,
    @Suppress("ANNOTATION_WILL_BE_APPLIED_ALSO_TO_PROPERTY_OR_FIELD")
    @ProtocolType
    val protocolType: Int = NsdManager.PROTOCOL_DNS_SD
)
