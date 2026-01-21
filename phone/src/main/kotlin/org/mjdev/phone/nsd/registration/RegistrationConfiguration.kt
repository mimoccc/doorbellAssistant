/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.nsd.registration

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import org.mjdev.phone.helpers.json.DontSerialize
import org.mjdev.phone.helpers.json.Serializable
import org.mjdev.phone.nsd.manager.ProtocolType

@Serializable
data class RegistrationConfiguration(
    val nsdServiceInfo: NsdServiceInfo,
    @Suppress("ANNOTATION_WILL_BE_APPLIED_ALSO_TO_PROPERTY_OR_FIELD")
    @ProtocolType
    val protocolType: Int = NsdManager.PROTOCOL_DNS_SD,
) {
    companion object {
        @DontSerialize
        val RegistrationConfiguration.serviceName: String?
            get() = nsdServiceInfo.serviceName

        @DontSerialize
        val RegistrationConfiguration.serviceType: String?
            get() = nsdServiceInfo.serviceType

        @DontSerialize
        val RegistrationConfiguration.port
            get() = nsdServiceInfo.port
    }
}
