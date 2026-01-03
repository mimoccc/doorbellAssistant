package org.mjdev.doorbellassistant.manager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import org.mjdev.doorbellassistant.R

@Suppress("unused")
class AppNotificationManager(
    private val context: Context,
    private val iconResId: Int = R.mipmap.ic_launcher,
    private val titleResId: Int = R.string.app_name
) {
    private val messages: MutableMap<Any, NotificationData> = mutableMapOf()
    private val channelId = "${context.packageName}.notifications"

    val notificationId = 1

    private val androidManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

    init {
        createNotificationChannel()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(titleResId),
                AndroidNotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service notifications"
                setShowBadge(false)
            }
            androidManager.createNotificationChannel(channel)
        }
    }

    fun createForegroundServiceNotification(
        key: Any,
        data: NotificationData
    ): Notification {
        messages[key] = data
        return buildNotification()
    }

    fun addNotification(
        key: Any,
        data: NotificationData
    ) {
        messages[key] = data
        updateNotification()
    }

    fun removeNotification(key: Any) {
        messages.remove(key)
        updateNotification()
    }

    private fun buildNotification(): Notification {
        val title = context.getString(titleResId)
        val content = buildContent()
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(iconResId)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(createBigTextStyle(content))
            .build()
    }

    private fun createBigTextStyle(
        content: String
    ) = NotificationCompat.BigTextStyle().bigText(content)

    private fun buildContent() = when {
        messages.isEmpty() -> ""
        messages.size == 1 -> messages.values.first().let { "${it.title}: ${it.message}" }
        else -> messages.values.joinToString("\n") { "${it.title}: ${it.message}" }
    }

    private fun updateNotification() {
        androidManager.notify(notificationId, buildNotification())
    }

    data class NotificationData(
        val title: String,
        val message: String
    )

//    fun test (context:Context) {
//        val notificationManager = AppNotificationManager(context)
//        // Pro foreground service
//        val notification = notificationManager.createForegroundServiceNotification(
//            key = "service",
//            data = NotificationData("Status", "Service běží")
//        )
//        context.startForeground(notificationManager.notificationId, notification)
//        // Přidání další notifikace
//        notificationManager.addNotification(
//            key = "connection",
//            data = NotificationData("Připojení", "Aktivní")
//        )
//        // Odstranění notifikace
//        notificationManager.removeNotification("connection")
//    }
}
