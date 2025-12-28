package org.mjdev.doorbellassistant.nsd.manager

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo

internal class NsdManagerCompatImpl(
    private val nsdManager: NsdManager
) : NsdManagerCompat {
    override fun registerService(
        serviceInfo: NsdServiceInfo,
        protocolType: Int,
        listener: NsdManager.RegistrationListener
    ) {
        runCatching {
            nsdManager.registerService(serviceInfo, protocolType, listener)
        }
    }

    override fun unregisterService(
        listener: NsdManager.RegistrationListener
    ) {
        runCatching {
            nsdManager.unregisterService(listener)
        }
    }


    override fun discoverServices(
        serviceType: String,
        protocolType: Int,
        listener: NsdManager.DiscoveryListener
    ) {
        runCatching {
            nsdManager.discoverServices(serviceType, protocolType, listener)
        }
    }

    override fun stopServiceDiscovery(
        listener: NsdManager.DiscoveryListener
    ) {
        runCatching {
            nsdManager.stopServiceDiscovery(listener)
        }
    }

    @Suppress("DEPRECATION")
    override fun resolveService(
        serviceInfo: NsdServiceInfo,
        listener: NsdManager.ResolveListener
    ) {
        runCatching {
            nsdManager.resolveService(serviceInfo, listener)
        }
    }

    companion object {
        fun fromContext(
            context: Context
        ) = NsdManagerCompatImpl(context.getSystemService(Context.NSD_SERVICE) as NsdManager)
    }
}
