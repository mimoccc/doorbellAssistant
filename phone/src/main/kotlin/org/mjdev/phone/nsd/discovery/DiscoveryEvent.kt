/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.nsd.discovery

import android.net.nsd.NsdServiceInfo
import org.mjdev.phone.helpers.json.Serializable

@Serializable
open class DiscoveryEvent {
    @Serializable
    data class DiscoveryServiceFound(
        val service: NsdServiceInfo
    ) : DiscoveryEvent()

    @Serializable
    data class DiscoveryServiceLost(
        val service: NsdServiceInfo
    ) : DiscoveryEvent()

    @Serializable
    data class DiscoveryStarted(
        val registeredType: String
    ) : DiscoveryEvent()

    @Serializable
    data class DiscoveryStopped(
        val serviceType: String
    ) : DiscoveryEvent()
}
