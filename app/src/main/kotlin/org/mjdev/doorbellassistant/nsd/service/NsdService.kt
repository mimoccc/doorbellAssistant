package org.mjdev.doorbellassistant.nsd.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.LazyDI
import org.mjdev.doorbellassistant.R
import org.mjdev.doorbellassistant.di.mainDI
import org.mjdev.doorbellassistant.enums.ChannelId
import org.mjdev.doorbellassistant.enums.NotificationId
import org.mjdev.doorbellassistant.extensions.ComposeExt.ANDROID_ID
import org.mjdev.doorbellassistant.nsd.device.NsdTypes
import org.mjdev.doorbellassistant.nsd.device.NsdTypes.Companion.serviceName
import org.mjdev.doorbellassistant.nsd.manager.NsdManagerFlow
import org.mjdev.doorbellassistant.nsd.registration.RegistrationEvent
import org.mjdev.doorbellassistant.nsd.rpc.EmptyRPCServer
import org.mjdev.doorbellassistant.nsd.rpc.INsdServerRPC
import kotlin.uuid.ExperimentalUuidApi

@SuppressLint("HardwareIds")
@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
abstract class NsdService : LifecycleService(), DIAware {
    override val di: LazyDI by mainDI(this)

    // todo
//    private val notificationManager : AppNotificationManager by di.instance()

    private val nsdManagerFlow by lazy {
        NsdManagerFlow(this)
    }
    private var registrationJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    open val serviceType: NsdTypes = NsdTypes.UNSPECIFIED
    open val port: Int = 0
    open val rpcServer: INsdServerRPC by lazy { EmptyRPCServer(baseContext) }

    val powerManager
        get() = getSystemService(Context.POWER_SERVICE) as PowerManager

    override fun onCreate() {
        startAsForeground()
        super.onCreate()
        if (port > 0) {
            registerNsdService()
            startRpcServer()
        }
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
    }

    private fun startRpcServer() {
        lifecycleScope.launch {
            rpcServer.start()
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
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentTitle("NSD Service")
            .setContentText("Running...")
            .build()
    }.getOrElse {
        NotificationCompat.Builder(this, ChannelId.NSD.id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    protected fun registerNsdService(
        onRegistered: (RegistrationEvent) -> Unit = {}
    ) {
        if (port <= 0) {
            Log.e(TAG, "Cannot register NSD service: port is $port")
            return
        }
        Log.d(TAG, "Registering NSD service: $ANDROID_ID, $serviceType, $port")
        registrationJob?.cancel()
        registrationJob = nsdManagerFlow
            .registerService(
                serviceName = ANDROID_ID,
                serviceType = serviceType.serviceName,
                port = port
            )
            .onEach { event ->
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