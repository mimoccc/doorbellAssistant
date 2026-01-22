/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.nsd.device

import android.content.Context
import android.net.nsd.NsdServiceInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mjdev.phone.nsd.device.NsdType.Companion.serviceTypeName
import org.mjdev.phone.nsd.discovery.DiscoveryConfiguration
import org.mjdev.phone.nsd.discovery.DiscoveryEvent
import org.mjdev.phone.nsd.manager.NsdManagerFlow
import org.mjdev.phone.nsd.resolve.ResolveEvent

@Composable
fun rememberNsdDeviceList(
    types: List<NsdType> = NsdType.entries,
    onError: (Throwable) -> Unit = {},
    filter: (NsdServiceInfo) -> Boolean = { true }
): State<List<NsdDevice>> {
    val context: Context = LocalContext.current
    return remember(types, filter) {
        createNsdDeviceFlow(
            context = context,
            types = types,
            filter = filter,
            onError = onError
        )
    }.collectAsState()
}

@OptIn(ExperimentalCoroutinesApi::class)
fun createNsdDeviceFlow(
    context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    types: List<NsdType> = NsdType.entries,
    onError: (Throwable) -> Unit = {},
    filter: (NsdServiceInfo) -> Boolean = { true }
): StateFlow<List<NsdDevice>> {
    val devicesFlow = MutableStateFlow(emptyList<NsdDevice>())
    val nsdFlow = NsdManagerFlow(context)
    val mutex = Mutex()
    scope.launch {
        nsdFlow
            .discoverServices(types.map { type ->
                DiscoveryConfiguration(type.serviceTypeName)
            })
            .catch { e ->
                onError(e)
            }
            .collect { event ->
                when (event) {
                    is DiscoveryEvent.DiscoveryServiceFound -> scope.launch {
                        mutex.withLock {
                            nsdFlow
                                .resolveService(event.service)
                                .take(1)
                                .catch { e ->
                                    onError(e)
                                }
                                .collect { re ->
                                    if (re is ResolveEvent.ServiceResolved && filter(re.nsdServiceInfo)) {
                                        val device = NsdDevice(re.nsdServiceInfo)
                                        devicesFlow.update { list ->
                                            list.filterNot { d ->
                                                d.address == device.address
                                            }.filterNot { d ->
                                                d.serviceId == device.serviceId
                                            } + device
                                        }
                                    }
                                }
                        }
                    }

                    is DiscoveryEvent.DiscoveryServiceLost -> {
                        devicesFlow.update { list ->
                            list.filterNot { d ->
                                d.serviceId == event.service.serviceName
                            }.distinctBy { d ->
                                d.address
                            }
                        }
                    }

                    else -> {}
                }
            }
    }
    return devicesFlow
}
