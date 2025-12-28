package org.mjdev.doorbellassistant.nsd.resolve

import android.net.nsd.NsdServiceInfo

sealed class ResolveEvent {
    data class ServiceResolved(
        val nsdServiceInfo: NsdServiceInfo
    ) : ResolveEvent()
}
