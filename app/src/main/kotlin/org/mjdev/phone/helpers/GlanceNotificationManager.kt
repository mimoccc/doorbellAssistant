@file:OptIn(ExperimentalGlanceApi::class, ExperimentalGlancePreviewApi::class)

package org.mjdev.phone.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.runComposition
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.R
import org.mjdev.doorbellassistant.ui.theme.DarkMD5
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Preview(640, 300)
@GlanceComposable
@Composable
fun TestNotification() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(DarkMD5)
            .cornerRadius(16.dp)
            .padding(4.dp)
    ) {
        Text(
            modifier = GlanceModifier.fillMaxWidth(),
            text = "Hello world",
        )
        val items = (0..32).map { it }
        Column(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            items.forEach { i ->
                Text("Item $i")
            }
        }
    }
}

@Suppress("unused")
class GlanceNotificationManager(
    private val context: Context
) {
    val notificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showGlanceNotification(
        channelId: String = CHANNEL_ID,
        channelName: String = channelId,
        visibility: Int = NotificationCompat.VISIBILITY_PUBLIC,
        importance: Int = NotificationManager.IMPORTANCE_MIN,
        showBadge: Boolean = true,
        content: @GlanceComposable @Composable () -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            GlanceNotificationWidget(content).runComposition(
                context = context,
                options = Bundle()
            ).collectLatest { remoteViews ->
                createNotificationChannel(channelId, channelName)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(remoteViews)
                        .build()
                )
            }
        }
    }

    private fun createNotificationChannel(
        channelId: String = CHANNEL_ID,
        channelName: String = channelId,
        visibility: Int = NotificationCompat.VISIBILITY_PUBLIC,
        importance: Int = NotificationManager.IMPORTANCE_MIN,
        showBadge: Boolean = true
    ) {
        NotificationChannel(channelId, channelName, importance).apply {
            lockscreenVisibility = visibility
            setShowBadge(showBadge)
        }.also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

    class GlanceNotificationWidget(
        val content: @GlanceComposable @Composable () -> Unit = {}
    ) : GlanceAppWidget() {
        override suspend fun provideGlance(context: Context, id: GlanceId) {
            provideContent {
                content()
            }
        }
    }

    fun test() = showGlanceNotification {
        TestNotification()
    }

    companion object {
        private const val CHANNEL_ID = "glance_channel"
        private const val NOTIFICATION_ID = 1
    }
}


