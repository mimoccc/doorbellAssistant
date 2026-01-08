package org.mjdev.phone.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.annotation.ColorLong
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.java

@Suppress("DEPRECATION", "unused")
object CustomExtensions {

    @SuppressLint("NewApi")
    fun Activity.dismissKeyguard() {
        runCatching {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    fun Activity.setFullScreen() {
        runCatching {
            setTurnScreenOn(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setShowWhenLocked(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    @SuppressLint("NewApi")
    fun Activity.turnDisplayOn() {
        runCatching {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setTurnScreenOn(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setShowWhenLocked(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    fun Activity.hideSystemBars() = runCatching {
        WindowCompat.getInsetsController(window, window.decorView).also { windowInsetsController ->
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }.onFailure { e ->
        e.printStackTrace()
    }

    @SuppressLint("NewApi")
    fun ComponentActivity.turnDisplayOff() {
        runCatching {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setTurnScreenOn(false)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setShowWhenLocked(false)
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    @SuppressLint("NewApi")
    fun ComponentActivity.turnDisplayOn() {
        runCatching {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setTurnScreenOn(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setShowWhenLocked(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    inline fun <reified T:Activity> T.bringToFront() {
        runCatching {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val myPackage = T::class.java.`package`?.name
            val task = activityManager.appTasks.firstOrNull { task ->
                task.taskInfo.baseActivity?.packageName == myPackage
            }
            task?.moveToFront()
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    val Context.ANDROID_ID: String
        @SuppressLint("HardwareIds")
        get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

    @Suppress("DEPRECATION")
    val Context.currentWifiSSID: String
        get() = run {
            val wifiManager =
                (applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)
            var ssid = wifiManager?.connectionInfo?.ssid?.replace("\"", "")
            if (ssid == null || ssid == "<unknown ssid>" || ssid == "unknown") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val connectivityManager = getSystemService(
                        Context.CONNECTIVITY_SERVICE
                    ) as? ConnectivityManager
                    val network = connectivityManager?.activeNetwork
                    val capabilities = connectivityManager?.getNetworkCapabilities(network)
                    if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                        val wifiInfo = capabilities.transportInfo as? WifiInfo
                        ssid = wifiInfo?.ssid?.replace("\"", "")
                    }
                }
            }
            if (ssid == null || ssid == "<unknown ssid>") {
                "unknown"
            } else {
                ssid
            }
        }

    val Context.currentWifiIP: String
        get() = runCatching {
            NetworkInterface.getNetworkInterfaces()
                .toList()
                .firstOrNull { n ->
                    n.name.startsWith("wlan") && n.isUp
                }
                ?.inetAddresses
                ?.toList()
                ?.filterIsInstance<Inet4Address>()
                ?.firstOrNull { n ->
                    n.isSiteLocalAddress
                }?.hostAddress
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull() ?: "..."

    val Context.currentSystemUser: String
        get() = runCatching {
            @Suppress("DEPRECATION")
            Settings.Secure.getString(contentResolver, "user_name") ?: "Unknown user"
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull() ?: "System User"

    inline fun <reified T> Context.intent(
        block: Intent.() -> Unit
    ): Intent = Intent(applicationContext, T::class.java).apply(block)

    fun String.toInetAddress(): InetAddress? = when {
        isValidIpAddress() -> runCatching {
            InetAddress.getByName(this)
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()

        else -> {
            Exception("Invalid ip address: $this").printStackTrace()
            null
        }
    }

    fun String.isValidIpAddress(): Boolean = split(".").let { parts ->
        parts.size == 4 && parts.all { it.toIntOrNull() in 0..255 }
    }

    fun LifecycleOwner.launchOnLifecycle(
        scope: LifecycleCoroutineScope = lifecycleScope,
        context: CoroutineContext = Dispatchers.Main,
        block: suspend CoroutineScope.() -> Unit
    ) = scope.launch(
        context = context,
        block = block
    )

    fun Modifier.applyIf(
        condition: Boolean,
        other: Modifier.() -> Modifier
    ): Modifier = if (condition) this.then(other()) else this

    fun ComponentActivity.enableEdgeToEdge(
        statusBarColor: Color = Color.DarkGray,
        navigationBarColor: Color = Color.DarkGray,
    ) = enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(statusBarColor.toColorInt()),
        navigationBarStyle = SystemBarStyle.dark(navigationBarColor.toColorInt())
    )

    @ColorLong
    fun Color.toColorLong(): Long {
        return if ((value and 0x3FUL) < 16UL) {
            value
        } else {
            (value and 0x3FUL.inv()) or ((value and 0x3FUL) - 1UL)
        }.toLong()
    }

    @ColorInt
    fun Color.toColorInt(): Int {
        return if ((value and 0x3FUL) < 16UL) {
            value
        } else {
            (value and 0x3FUL.inv()) or ((value and 0x3FUL) - 1UL)
        }.toInt()
    }

    val isPreview
        get() = isLayoutLib()

    val isInPreviewMode: Boolean
        get() = isLayoutLib()

    fun isLayoutLib(): Boolean {
        val device = Build.DEVICE
        val product = Build.PRODUCT
        return (device == "layoutlib") || (product == "layoutlib")
    }

    @Composable
    fun rememberAssetImage(
        assetImageFile: String = "avatar_transparent.png",
        onError: (Throwable) -> ImageBitmap = { ImageBitmap(1, 1) },
    ): ImageBitmap {
        val context: Context = LocalContext.current
        return remember(assetImageFile) {
            runCatching {
                context.assets.open(assetImageFile).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream).asImageBitmap()
                }
            }.onFailure { e ->
                onError(e)
            }.getOrNull()
                ?: onError(RuntimeException("Error loading: $assetImageFile from assets."))
        }
    }

    @Composable
    fun rememberAssetImagePainter(
        assetImageFile: String = "avatar_transparent.png",
        assetImage: ImageBitmap = rememberAssetImage(
            assetImageFile = assetImageFile
        ),
    ): Painter = remember(assetImageFile, assetImage) {
        BitmapPainter(assetImage)
    }

    inline fun <reified T : Activity> Context.isFinished(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val myPackage = T::class.java.`package`?.name
        val task = activityManager.appTasks.firstOrNull { task ->
            task.taskInfo.baseActivity?.packageName == myPackage &&
                    task.taskInfo.baseActivity?.className == T::class.qualifiedName
        }
        return task == null
    }

    inline fun <reified T : Activity> Context.isRunning(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val myPackage = T::class.java.`package`?.name
        val task = activityManager.appTasks.firstOrNull { task ->
            task.taskInfo.baseActivity?.packageName == myPackage &&
                    task.taskInfo.baseActivity?.className == T::class.qualifiedName
        }
        return task != null
    }

    inline fun <reified T : Activity> startOrResume(
        context: Context
    ) {
        if (context.isFinished<T>()) {
            Intent(context, T::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            }.apply {
                context.startActivity(this)
            }
        }
    }

    @Composable
    fun rememberLifeCycleOwnerState(): State<LifecycleOwner?> {
        val lifecycleOwner = if (isPreview) null else LocalLifecycleOwner.current
        return rememberUpdatedState(lifecycleOwner)
    }

}
