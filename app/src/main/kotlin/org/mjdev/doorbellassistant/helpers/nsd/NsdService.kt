package org.mjdev.doorbellassistant.helpers.nsd

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.mjdev.doorbellassistant.R
import org.mjdev.doorbellassistant.enums.ChannelId
import org.mjdev.doorbellassistant.enums.NotificationId
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.Companion.serviceName
import org.mjdev.doorbellassistant.helpers.nsd.registration.RegistrationEvent
import kotlin.uuid.ExperimentalUuidApi

@SuppressLint("HardwareIds")
@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
abstract class NsdService : LifecycleService() {
    private val serviceID: String by lazy {
        Settings.Secure.getString(
            baseContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
    private var registrationJob: Job? = null
    private val nsdManagerFlow by lazy {
        NsdManagerFlow(this)
    }
    open val serviceType: NsdTypes  = NsdTypes.UNSPECIFIED
    open val port: Int = 0

    override fun onCreate() {
        startAsForeground()
        super.onCreate()
        if (port > 0) {
            registerNsdService()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        }
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
            Log.e("NsdService", "Cannot register NSD service: port is $port")
            return
        }
        val name = serviceID
        Log.d("NsdService", "Registering NSD service: $name, $serviceType, $port")
        registrationJob?.cancel()
        registrationJob = nsdManagerFlow
            .registerService(
                serviceName = name,
                serviceType = serviceType.serviceName,
                port = port
            )
            .onEach { event ->
                Log.d("NsdService", "NSD Registration event: $event")
                if (event is RegistrationEvent.ServiceRegistered) {
                    if (event.nsdServiceInfo.serviceName != name) {
                        Log.d("NsdService", "Service name changed from $name to ${event.nsdServiceInfo.serviceName}")
                    }
                }
                onRegistered(event)
            }.launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        registrationJob?.cancel()
        super.onDestroy()
    }
}