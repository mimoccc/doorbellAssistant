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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.mjdev.phone.enums.ChannelId
import org.mjdev.phone.enums.NotificationId
import org.mjdev.phone.extensions.CustomExtensions.ANDROID_ID
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.device.NsdTypes.Companion.serviceName
import org.mjdev.phone.nsd.manager.NsdManagerFlow
import org.mjdev.phone.nsd.registration.RegistrationEvent
import org.mjdev.phone.rpc.INsdServerRPC
import kotlin.uuid.ExperimentalUuidApi

@SuppressLint("HardwareIds")
@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
abstract class NsdService : BindableService() {
    // todo
//    private val notificationManager : AppNotificationManager by di.instance()

    private val nsdManagerFlow by lazy {
        NsdManagerFlow(this)
    }
    private var registrationJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    abstract val serviceType: NsdTypes
    abstract val rpcServer: INsdServerRPC

    val powerManager
        get() = getSystemService(POWER_SERVICE) as PowerManager

    override fun onCreate() {
        startAsForeground()
        super.onCreate()
        startRpcServer()
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
    }

    private fun startRpcServer() {
        lifecycleScope.launch {
            rpcServer.start(
                onStarted = { a, p ->
                    registerNsdService(a, p)
                }
            )
        }
    }

    private fun stopRpcServer() {
        lifecycleScope.launch {
            rpcServer.stop()
        }
    }

    private fun startAsForeground() {
        createNotificationChannel()
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NotificationId.NSD.id,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NotificationId.NSD.id, notification)
        }
    }

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
//            .setSmallIcon(R.mipmap.ic_launcher) // todo
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentTitle("NSD Service")
            .setContentText("Running...")
            .build()
    }.getOrElse {
        NotificationCompat.Builder(this, ChannelId.NSD.id)
//            .setSmallIcon(R.mipmap.ic_launcher) // todo
            .build()
    }

    @Suppress("unused")
    protected fun registerNsdService(
        address: String,
        port: Int,
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
        stopRpcServer()
        if (wakeLock?.isHeld == true) wakeLock?.release()
        super.onDestroy()
    }

    companion object {
        private val TAG = NsdService::class.simpleName
    }
}