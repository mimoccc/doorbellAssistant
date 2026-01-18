package org.mjdev.phone.nsd.device

import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.glance.color.DynamicThemeColorProviders.onError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mjdev.phone.nsd.device.NsdTypes.Companion.serviceName
import org.mjdev.phone.nsd.discovery.DiscoveryConfiguration
import org.mjdev.phone.nsd.discovery.DiscoveryEvent
import org.mjdev.phone.nsd.manager.NsdManagerFlow
import org.mjdev.phone.nsd.resolve.ResolveEvent

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun rememberNsdManagerFlow(): NsdManagerFlow {
    val context: Context = LocalContext.current
    return remember { NsdManagerFlow(context) }
}

@Composable
fun rememberNsdDeviceList(
    types: List<NsdTypes> = NsdTypes.entries,
    onError: (Throwable) -> Unit = {},
    filter: (NsdServiceInfo) -> Boolean = { true }
): SnapshotStateList<NsdDevice> {
    val context: Context = LocalContext.current
    return remember {
        createNsdDeviceList(
            context = context,
            types = types,
            filter = filter,
            onError = onError
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun createNsdDeviceList(
    context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    types: List<NsdTypes> = NsdTypes.entries,
    onError: (Throwable) -> Unit = {},
    filter: (NsdServiceInfo) -> Boolean = { true }
) = SnapshotStateList<NsdDevice>().apply {
    val nsdManagerFlow = NsdManagerFlow(context)
    val resolveMutex = Mutex()
    val serviceTypes = types.map { type ->
        DiscoveryConfiguration(
            type.serviceName
        )
    }
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
                                                if (filter(resolveEvent.nsdServiceInfo)) {
                                                    NsdDevice(resolveEvent.nsdServiceInfo).also { d ->
                                                        add(d)
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                    }

                    is DiscoveryEvent.DiscoveryServiceLost -> {
                        removeIf { d ->
                            d.serviceName == event.service.serviceName
                        }
                    }

                    else -> Unit
                }
            }
    }
}

//@Suppress("RedundantSuspendModifier")
//@OptIn(ExperimentalCoroutinesApi::class)
//suspend fun nsdDeviceListFlow(
//    context: Context,
//    types: List<NsdTypes> = NsdTypes.entries,
//    onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
//    filter: (NsdServiceInfo) -> Boolean = { true },
//): Flow<List<NsdDevice>> = flow {
//    val services = mutableStateListOf<NsdDevice>()
//    val resolveMutex = Mutex()
//    val nsdManagerFlow = NsdManagerFlow(context)
//    val types = types.filter { t ->
//        t != NsdTypes.DOOR_BELL_ASSISTANT
//    }.map { type ->
//        DiscoveryConfiguration(type.serviceName)
//    }
//    nsdManagerFlow.discoverServices(types).catch { e ->
//        Log.e("NsdList", "Discovery failed", e)
//        onError(e)
//    }.collect { event ->
//        when (event) {
//            is DiscoveryEvent.DiscoveryServiceFound -> {
//                resolveMutex.withLock {
//                    nsdManagerFlow
//                        .resolveService(event.service)
//                        .take(1)
//                        .catch { e -> onError(e) }
//                        .collect { resolveEvent ->
//                            when (resolveEvent) {
//                                is ResolveEvent.ServiceResolved -> {
//                                    if (filter(resolveEvent.nsdServiceInfo)) {
//                                        NsdDevice(resolveEvent.nsdServiceInfo).also { d ->
//                                            services.add(d)
//                                            emit(services)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                }
//            }
//
//            is DiscoveryEvent.DiscoveryServiceLost -> {
//                services.removeIf { d ->
//                    d.serviceName == event.service.serviceName
//                }
//                emit(services)
//            }
//
//            else -> {
//            }
//        }
//    }
//}
