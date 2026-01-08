package org.mjdev.phone.nsd.manager

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.mjdev.phone.nsd.discovery.DiscoveryConfiguration
import org.mjdev.phone.nsd.discovery.DiscoveryEvent
import org.mjdev.phone.nsd.discovery.DiscoveryListenerFlow
import org.mjdev.phone.nsd.registration.RegistrationConfiguration
import org.mjdev.phone.nsd.registration.RegistrationEvent
import org.mjdev.phone.nsd.registration.RegistrationListenerFlow
import org.mjdev.phone.nsd.resolve.ResolveEvent
import org.mjdev.phone.nsd.resolve.ResolveListenerFlow

@Suppress("unused")
@ExperimentalCoroutinesApi
class NsdManagerFlow(
    private val nsdManagerCompat: NsdManagerCompat
) {
    constructor(
        context: Context
    ) : this(NsdManagerCompatImpl.fromContext(context))

    fun discoverServices(
        discoveryConfiguration: DiscoveryConfiguration
    ): Flow<DiscoveryEvent> = callbackFlow {
        nsdManagerCompat.discoverServices(
            serviceType = discoveryConfiguration.type,
            protocolType = discoveryConfiguration.protocolType,
            listener = DiscoveryListenerFlow(this),
        )
        awaitClose()
    }

    fun discoverServices(
        discoveryConfigurations: List<DiscoveryConfiguration>
    ): Flow<DiscoveryEvent> = callbackFlow {
        discoveryConfigurations.forEach { discoveryConfiguration ->
            nsdManagerCompat.discoverServices(
                serviceType = discoveryConfiguration.type,
                protocolType = discoveryConfiguration.protocolType,
                listener = DiscoveryListenerFlow(this),
            )
        }
        awaitClose()
    }

    fun registerService(
        registrationConfiguration: RegistrationConfiguration
    ): Flow<RegistrationEvent> = callbackFlow {
        nsdManagerCompat.registerService(
            serviceInfo = NsdServiceInfo().apply {
                serviceName = registrationConfiguration.serviceName
                port = registrationConfiguration.port
                serviceType = registrationConfiguration.serviceType
            },
            protocolType = registrationConfiguration.protocolType,
            listener = RegistrationListenerFlow(this)
        )
        awaitClose()
    }

    fun registerService(
        serviceName: String = "default",
        serviceType: String = "_http._tcp.",
        port: Int,
        @ProtocolType
        protocolType: Int = NsdManager.PROTOCOL_DNS_SD
    ): Flow<RegistrationEvent> = callbackFlow {
        nsdManagerCompat.registerService(
            serviceInfo = NsdServiceInfo().apply {
                this.serviceName = serviceName
                this.port = port
                this.serviceType = serviceType
            },
            protocolType = protocolType,
            listener = RegistrationListenerFlow(this)
        )
        awaitClose()
    }

    fun resolveService(
        serviceInfo: NsdServiceInfo
    ): Flow<ResolveEvent> = callbackFlow {
        nsdManagerCompat.resolveService(
            serviceInfo = serviceInfo,
            listener = ResolveListenerFlow(this)
        )
        awaitClose()
    }
}
