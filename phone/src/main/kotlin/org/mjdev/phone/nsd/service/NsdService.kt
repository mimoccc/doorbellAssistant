/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.nsd.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.phone.R
import org.mjdev.phone.enums.ChannelId
import org.mjdev.phone.enums.NotificationId
import org.mjdev.phone.extensions.ContextExt.ANDROID_ID
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.device.NsdTypes.Companion.serviceName
import org.mjdev.phone.nsd.device.createNsdDeviceFlow
import org.mjdev.phone.nsd.manager.NsdManagerFlow
import org.mjdev.phone.nsd.registration.RegistrationEvent
import org.mjdev.phone.rpc.server.INsdServerRPC
import org.mjdev.phone.service.BindableService
import org.mjdev.phone.service.ServiceEvent
import kotlin.uuid.ExperimentalUuidApi

@SuppressLint("HardwareIds")
@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
abstract class NsdService(
    serviceNsdType: NsdTypes
) : BindableService() {
    // todo
//    private val notificationManager : AppNotificationManager by di.instance()

    protected val nsdManagerFlow by lazy {
        NsdManagerFlow(this)
    }
    protected val devicesAround by lazy {
        createNsdDeviceFlow(applicationContext)
    }
    private var registrationJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    var serviceType: NsdTypes = serviceNsdType

    abstract val rpcServer: INsdServerRPC

    val powerManager
        get() = getSystemService(POWER_SERVICE) as PowerManager

    override fun onCreate() {
        startAsForeground()
        super.onCreate()
        startRpcServer()
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
    }

    protected fun startRpcServer() {
        lifecycleScope.launch {
            rpcServer.start(
                onStarted = { a, p ->
                    registerNsdService(a, p, serviceType)
                }
            )
        }
    }

    protected suspend fun stopRpcServer() {
        lifecycleScope.launch {
            unregisterNsdService {
                rpcServer.stop()
            }
        }
    }

    private fun startAsForeground() {
        createNotificationChannel()
        val notification = createNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NotificationId.NSD.id,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NotificationId.NSD.id, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start as foreground service: ${e.message}", e)
        }
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel() = runCatching {
        val channel = NotificationChannel(
            ChannelId.NSD.id,
            "NSD Service",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "NSD Service notification"
            lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }.onFailure { e ->
        e.printStackTrace()
    }

    private fun createNotification() = runCatching {
        NotificationCompat.Builder(this, ChannelId.NSD.id)
            .setSmallIcon(R.mipmap.ic_launcher) // todo
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentTitle("NSD Service")
            .setContentText("Running...")
            .build()
    }.getOrElse {
        NotificationCompat.Builder(this, ChannelId.NSD.id)
            .setSmallIcon(R.mipmap.ic_launcher) // todo
            .build()
    }

    protected fun unregisterNsdService(
        onUnregistered: suspend () -> Unit = {}
    ) {
        lifecycleScope.launch {
            nsdManagerFlow.unregisterService().onEach { event ->
                Log.d(TAG, "NSD Registration event: $event")
            }.onEach { ev ->
                if (ev is RegistrationEvent.ServiceUnregistered) {
                    onUnregistered()
                }
            }.collect()
        }
    }

    @Suppress("unused")
    protected fun registerNsdService(
        address: String,
        port: Int,
        serviceType: NsdTypes,
        onRegistered: (RegistrationEvent) -> Unit = {}
    ) {
        if (port <= 0) {
            Log.e(TAG, "Cannot register NSD service: port is $port")
            return
        }
        Log.d(TAG, "Registering NSD service: $ANDROID_ID, $serviceType, $port")
        registrationJob?.cancel()
        registrationJob = nsdManagerFlow.registerService(
            serviceName = ANDROID_ID,
            serviceType = serviceType.serviceName,
            port = port
        ).onEach { event ->
            Log.d(TAG, "NSD Registration event: $event")
            if (event is RegistrationEvent.ServiceRegistered) {
                if (event.nsdServiceInfo.serviceName != ANDROID_ID) {
                    Log.d(
                        TAG,
                        "Service name changed from $ANDROID_ID to ${event.nsdServiceInfo.serviceName}"
                    )
                }
            }
            onRegistered(event)
        }.launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        registrationJob?.cancel()
        lifecycleScope.launch {
            stopRpcServer()
        }
        if (wakeLock?.isHeld == true) wakeLock?.release()
        super.onDestroy()
    }

    fun changeType(
        type: NsdTypes,
        onChanged: (NsdTypes, NsdTypes) -> Unit
    ) {
        Log.d(TAG, "changeType called: current=$serviceType, new=$type")
        if (serviceType != type) {
            Log.d(TAG, "Service types differ, proceeding with change")
            lifecycleScope.launch {
                Log.d(TAG, "Before stopRpcServer")
                stopRpcServer()
                Log.d(TAG, "After stopRpcServer")
                serviceType = type
                Log.d(TAG, "Service type changed to: $serviceType")
                startRpcServer()
                Log.d(TAG, "After startRpcServer")
                withContext(Dispatchers.Main) {
                    onChanged(serviceType, type)
                }
            }
        } else {
            Log.d(TAG, "Service types are the same, no change needed")
        }
    }

    companion object {
        private val TAG = NsdService::class.simpleName

        data class NsdStateEvent(
            val address: String,
            val port: Int
        ) : ServiceEvent()

        data class NsdDeviceEvent(
            val device: NsdDevice?
        ) : ServiceEvent()

        data class NsdDevicesEvent(
            val devicesAround: List<NsdDevice>
        ) : ServiceEvent()
    }
}
