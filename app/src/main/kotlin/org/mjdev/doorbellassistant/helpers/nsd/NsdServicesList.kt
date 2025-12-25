package org.mjdev.doorbellassistant.helpers.nsd

import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.Companion.serviceName
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_CLIENT
import org.mjdev.doorbellassistant.helpers.nsd.discovery.DiscoveryConfiguration
import org.mjdev.doorbellassistant.helpers.nsd.discovery.DiscoveryEvent
import org.mjdev.doorbellassistant.helpers.nsd.resolve.ResolveEvent

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun rememberNsdManagerFlow(
    context: Context = LocalContext.current
) = remember { NsdManagerFlow(context) }

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun rememberNsdServicesList(
    scope: CoroutineScope = rememberCoroutineScope(),
    nsdManagerFlow: NsdManagerFlow = rememberNsdManagerFlow(),
    onError: (Throwable) -> Unit = {},
    types: List<NsdTypes> = listOf(DOOR_BELL_ASSISTANT, DOOR_BELL_CLIENT),
) = remember {
    val services = mutableStateListOf<NsdServiceInfo>()
    val resolveMutex = Mutex()
    val serviceTypes = types.map { type -> DiscoveryConfiguration(type.serviceName) }
    scope.launch {
        nsdManagerFlow
            .discoverServices(serviceTypes)
            .catch { e ->
                Log.e("NsdList", "Discovery failed", e)
                onError(e)
            }
            .collect { event ->
                when (event) {
                    is DiscoveryEvent.DiscoveryServiceFound -> {
                        scope.launch {
                            resolveMutex.withLock {
                                nsdManagerFlow
                                    .resolveService(event.service)
                                    .take(1)
                                    .catch { e -> onError(e) }
                                    .collect { resolveEvent ->
                                        when (resolveEvent) {
                                            is ResolveEvent.ServiceResolved -> {
                                                services.add(resolveEvent.nsdServiceInfo)
                                            }
                                        }
                                    }
                            }
                        }
                    }

                    is DiscoveryEvent.DiscoveryServiceLost -> {
                        services.removeIf { it.serviceName == event.service.serviceName }
                    }

                    else -> Unit
                }
            }
    }
    services
}