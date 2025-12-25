package org.mjdev.doorbellassistant.helpers.nsd.registration

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.trySendBlocking
import org.mjdev.doorbellassistant.helpers.nsd.RegistrationFailed
import org.mjdev.doorbellassistant.helpers.nsd.UnregistrationFailed

internal class RegistrationListenerFlow(
    private val producerScope: ProducerScope<RegistrationEvent>
) : NsdManager.RegistrationListener {
    override fun onRegistrationFailed(
        nsdServiceInfo: NsdServiceInfo,
        errorCode: Int
    ) {
        runCatching {
            producerScope.channel.close(RegistrationFailed(nsdServiceInfo, errorCode))
        }
    }

    override fun onUnregistrationFailed(
        nsdServiceInfo: NsdServiceInfo,
        errorCode: Int
    ) {
        runCatching {
            producerScope.channel.close(UnregistrationFailed(nsdServiceInfo, errorCode))
        }
    }

    override fun onServiceRegistered(
        nsdServiceInfo: NsdServiceInfo
    ) {
        runCatching {
            producerScope.trySendBlocking(RegistrationEvent.ServiceRegistered(nsdServiceInfo))
        }
    }

    override fun onServiceUnregistered(
        nsdServiceInfo: NsdServiceInfo
    ) {
        runCatching {
            producerScope.trySendBlocking(RegistrationEvent.ServiceUnregistered(nsdServiceInfo))
        }
    }
}
