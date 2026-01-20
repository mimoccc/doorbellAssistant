//@file:OptIn(ExperimentalGlanceApi::class, ExperimentalGlancePreviewApi::class)
//
//package org.mjdev.phone.helpers
//
//import androidx.glance.ExperimentalGlanceApi
//import androidx.glance.preview.ExperimentalGlancePreviewApi
//
//@Preview(640, 300)
//@GlanceComposable
//@Composable
//fun TestNotification() {
//    Column(
//        modifier = GlanceModifier
//            .fillMaxSize()
//            .background(Black)
//            .cornerRadius(16.dp)
//            .padding(4.dp)
//    ) {
//        Text(
//            modifier = GlanceModifier.fillMaxWidth(),
//            text = "Hello world",
//        )
//        val items = (0..32).map { it }
//        Column(
//            modifier = GlanceModifier.fillMaxWidth()
//        ) {
//            items.forEach { i ->
//                Text("Item $i")
//            }
//        }
//    }
//}
//
//@Suppress("unused")
//class GlanceNotificationManager(
//    private val context: Context
//) {
//    val notificationManager
//        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//    fun showGlanceNotification(
//        channelId: String = CHANNEL_ID,
//        channelName: String = channelId,
//        visibility: Int = NotificationCompat.VISIBILITY_PUBLIC,
//        importance: Int = NotificationManager.IMPORTANCE_MIN,
//        showBadge: Boolean = true,
//        content: @GlanceComposable @Composable () -> Unit = {}
//    ) {
//        CoroutineScope(Dispatchers.Main).launch {
//            GlanceNotificationWidget(content).runComposition(
//                context = context,
//                options = Bundle()
//            ).collectLatest { remoteViews ->
//                createNotificationChannel(channelId, channelName)
//                notificationManager.notify(
//                    NOTIFICATION_ID,
//                    NotificationCompat.Builder(context, channelId)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//                        .setCustomContentView(remoteViews)
//                        .build()
//                )
//            }
//        }
//    }
//
//    private fun createNotificationChannel(
//        channelId: String = CHANNEL_ID,
//        channelName: String = channelId,
//        visibility: Int = NotificationCompat.VISIBILITY_PUBLIC,
//        importance: Int = NotificationManager.IMPORTANCE_MIN,
//        showBadge: Boolean = true
//    ) {
//        NotificationChannel(channelId, channelName, importance).apply {
//            lockscreenVisibility = visibility
//            setShowBadge(showBadge)
//        }.also { channel ->
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    class GlanceNotificationWidget(
//        val content: @GlanceComposable @Composable () -> Unit = {}
//    ) : GlanceAppWidget() {
//        override suspend fun provideGlance(context: Context, id: GlanceId) {
//            provideContent {
//                content()
//            }
//        }
//    }
//
//    fun test() = showGlanceNotification {
//        TestNotification()
//    }
//
//    companion object {
//        private const val CHANNEL_ID = "glance_channel"
//        private const val NOTIFICATION_ID = 1
//    }
//}
//
//
