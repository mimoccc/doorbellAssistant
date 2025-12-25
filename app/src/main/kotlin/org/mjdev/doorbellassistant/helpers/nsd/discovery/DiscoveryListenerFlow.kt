package org.mjdev.doorbellassistant.helpers.nsd.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.trySendBlocking
import org.mjdev.doorbellassistant.helpers.nsd.DiscoveryStartFailed
import org.mjdev.doorbellassistant.helpers.nsd.DiscoveryStopFailed

@ExperimentalCoroutinesApi
internal data class DiscoveryListenerFlow(
    private val producerScope: ProducerScope<DiscoveryEvent>,
) : NsdManager.DiscoveryListener {
    override fun onServiceFound(
        serviceInfo: NsdServiceInfo
    ) {
        producerScope.trySendBlocking(DiscoveryEvent.DiscoveryServiceFound(service = serviceInfo))
    }

    override fun onStopDiscoveryFailed(
        serviceType: String,
        errorCode: Int
    ) {
        producerScope.channel.close(DiscoveryStopFailed(serviceType, errorCode))
    }

    override fun onStartDiscoveryFailed(
        serviceType: String,
        errorCode: Int
    ) {
        producerScope.channel.close(DiscoveryStartFailed(serviceType, errorCode))
    }

    override fun onDiscoveryStarted(
        serviceType: String
    ) {
        producerScope.trySendBlocking(DiscoveryEvent.DiscoveryStarted(registeredType = serviceType))
    }

    override fun onDiscoveryStopped(
        serviceType: String
    ) {
        producerScope.trySendBlocking(DiscoveryEvent.DiscoveryStopped(serviceType = serviceType))
        producerScope.channel.close()
    }

    override fun onServiceLost(
        service: NsdServiceInfo
    ) {
        producerScope.trySendBlocking(DiscoveryEvent.DiscoveryServiceLost(service = service))
    }
}
