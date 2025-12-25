package org.mjdev.doorbellassistant.helpers.nsd.resolve

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.trySendBlocking
import org.mjdev.doorbellassistant.helpers.nsd.ResolveFailed

internal class ResolveListenerFlow(
    private val producerScope: ProducerScope<ResolveEvent>
) : NsdManager.ResolveListener {
    override fun onResolveFailed(
        nsdServiceInfo: NsdServiceInfo,
        errorCode: Int
    ) {
        runCatching {
            producerScope.channel.close(cause = ResolveFailed(nsdServiceInfo, errorCode))
        }
    }

    override fun onServiceResolved(
        nsdServiceInfo: NsdServiceInfo
    ) {
        runCatching {
            producerScope.trySendBlocking(ResolveEvent.ServiceResolved(nsdServiceInfo))
        }
    }
}
