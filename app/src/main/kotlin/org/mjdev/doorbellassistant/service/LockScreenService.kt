@file:Suppress("unused")

package org.mjdev.doorbellassistant.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.delay
import org.mjdev.doorbellassistant.activity.AssistantActivity
import org.mjdev.doorbellassistant.activity.AssistantActivity.Companion.isAppSetAsHomeLauncher
import org.mjdev.phone.extensions.CustomExtensions.startOrResume
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class LockScreenService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    private val windowManager by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }
    private val screenReceiver = ScreenReceiver()
    private var lockScreenView: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        registerScreenReceiver()
        startForegroundNotification()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        unregisterReceiver(screenReceiver)
//        hideLockScreen()
    }

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    @SuppressLint("InlinedApi")
    private fun startForegroundNotification() {
        val channelId = "lockscreen_service"
        val channel = NotificationChannel(
            channelId,
            "Lock Screen Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Lock Screen Active")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(1, notification)
        }
    }

    fun showLockScreen() {
        if(isAppSetAsHomeLauncher) {
            startOrResume<AssistantActivity>(baseContext.applicationContext)
        }
//        if (lockScreenView != null) return
//        lockScreenView = ComposeView(this).apply {
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//            setViewTreeLifecycleOwner(this@LockScreenService)
//            setViewTreeSavedStateRegistryOwner(this@LockScreenService)
//            setContent {
//                LockScreenContent(onUnlock = ::hideLockScreen)
//            }
//        }
//        windowManager.addView(lockScreenView, createWindowParams())
    }

//    fun hideLockScreen() {
//        lockScreenView?.let {
//            windowManager.removeView(it)
//            lockScreenView = null
//        }
//    }

//    private fun createWindowParams() = WindowManager.LayoutParams(
//        WindowManager.LayoutParams.MATCH_PARENT,
//        WindowManager.LayoutParams.MATCH_PARENT,
//        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
//                WindowManager.LayoutParams.FLAG_FULLSCREEN or
//                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
//        PixelFormat.TRANSLUCENT
//    )

    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> showLockScreen()
//                Intent.ACTION_USER_PRESENT -> hideLockScreen()
            }
        }
    }

//    @Previews
//    @Composable
//    fun LockScreenContent(
//        onUnlock: () -> Unit = {}
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Black),
//            contentAlignment = Alignment.Center
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = currentTime(),
//                    fontSize = 48.sp,
//                    color = White
//                )
//                Spacer(modifier = Modifier.height(32.dp))
//                Button(
//                    onClick = onUnlock,
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = White,
//                        contentColor = Black
//                    )
//                ) {
//                    Text("Unlock")
//                }
//            }
//        }
//    }

    @Composable
    private fun currentTime(): String {
        var time by remember { mutableStateOf(getCurrentTime()) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                time = getCurrentTime()
            }
        }
        return time
    }

    private fun getCurrentTime() =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    companion object {
        // todo screen off is screen off
//        fun Context.startLockScreenService() {
//            startService(Intent(this, LockScreenService::class.java))
//        }
    }
}
