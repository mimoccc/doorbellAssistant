package org.mjdev.doorbellassistant.extensions

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import org.mjdev.doorbellassistant.BuildConfig
import org.mjdev.doorbellassistant.R
import org.mjdev.doorbellassistant.enums.IntentAction
import org.mjdev.doorbellassistant.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.nsd.device.NsdDevice.Companion.TAG
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver
import org.mjdev.doorbellassistant.rpc.DoorBellAssistantServerRpc.Companion.getFrame
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.doorbellassistant.ui.theme.DarkMD5
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.coroutines.CoroutineContext
import androidx.compose.ui.graphics.Color as ComposeColor

@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION", "unused", "UnusedReceiverParameter")
object ComposeExt {

    private val TAG_WAKE_LOCK: Int = R.string.wake_lock

    val isPreview
        @Composable
        get()= LocalInspectionMode.current

    val isInPreviewMode: Boolean
        get() = isLayoutLib()

    fun isLayoutLib(): Boolean {
        val device = android.os.Build.DEVICE
        val product = android.os.Build.PRODUCT
        return device == "layoutlib" || product == "layoutlib"
    }

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
        prettyPrint = true
        isLenient = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
//        explicitNulls = true
//        coerceInputValues = true
//        decodeEnumsCaseInsensitive = true
//        allowStructuredMapKeys = true
    }

    inline fun <reified T : Any> T.asJson(): String {
        val json = json.encodeToString<T>(this)
        Log.d(TAG, "Device json : $json")
        return json
    }

    inline fun <reified T> String.fromJson(): T = runCatching {
        val result = json.decodeFromString<T>(this)
        Log.d(TAG, "Device from json : $json, $result")
        return result
    }.getOrNull() as T

    fun String.toInetAddress(): InetAddress? = when {
        isValidIpAddress() -> InetAddress.getByAddress(toByteArray())
        else -> {
            Exception("Invalid ip address: $this.").printStackTrace()
            null
        }
    }

    fun String.isValidIpAddress(): Boolean =
        split(".").let { parts ->
            parts.size == 4 && parts.all { it.toIntOrNull() in 0..255 }
        }

    fun String.toByteArray(): ByteArray =
        split(".").map { it.toInt().toByte() }.toByteArray()

    fun ImageVector.toDrawable(
        context: Context,
        width: Int,
        height: Int
    ): BitmapDrawable {
        val computedWidth = if (width < 1) 1 else width
        val computedHeight = if (height < 1) 1 else height
        val bitmap = createBitmap(computedWidth, computedHeight)
        val canvas = Canvas(bitmap)
        val vectorDrawable = VectorDrawable()
        vectorDrawable.setBounds(0, 0, computedWidth, computedHeight)
        vectorDrawable.draw(canvas)
        return BitmapDrawable(context.resources, bitmap)
    }

    fun ImageVector.toDrawable(
        view: View,
        width: Int = view.width,
        height: Int = view.height
    ): BitmapDrawable {
        val computedWidth = if (width < 1) 1 else width
        val computedHeight = if (height < 1) 1 else height
        val bitmap = createBitmap(computedWidth, computedHeight)
        val canvas = Canvas(bitmap)
        val vectorDrawable = VectorDrawable()
        vectorDrawable.setBounds(0, 0, computedWidth, computedHeight)
        vectorDrawable.draw(canvas)
        return BitmapDrawable(view.context.resources, bitmap)
    }

    var ComponentActivity.wakeLock: WakeLock?
        get() = runCatching {
            window.decorView.getTag(TAG_WAKE_LOCK) as? WakeLock
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()
        set(value) {
            window.decorView.setTag(TAG_WAKE_LOCK, value)
        }

    val Context.ANDROID_ID: String
        @SuppressLint("HardwareIds")
        get() {
            return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        }

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

    val Context.wifiManager
        get() = getSystemService(Context.WIFI_SERVICE) as? WifiManager

    val Context.connectivityManager
        get() = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    val Context.allNetworks: Map<Network, NetworkCapabilities?>
        get() = connectivityManager?.allNetworks?.associate { n ->
            n to connectivityManager?.getNetworkCapabilities(n)
        } ?: emptyMap()

//    val Context.currentPublicIP: String
//        get() = NetworkInterface.getNetworkInterfaces()
//            .toList()
//            .asSequence()
//            .filter { n ->
//                n.isUp && !n.isLoopback
//            }
//            .flatMap { n ->
//                n.inetAddresses.asSequence()
//            }
//            .firstOrNull { n ->
//                n is Inet4Address && !n.isLoopbackAddress
//            }?.hostAddress ?: "..."

    val Context.currentWifiIP: String
        get() = NetworkInterface.getNetworkInterfaces()
            .toList()
            .firstOrNull { n ->
                n.name.startsWith("wlan") && n.isUp
            }
            ?.inetAddresses
            ?.toList()
            ?.filterIsInstance<Inet4Address>()
            ?.firstOrNull { n ->
                n.isSiteLocalAddress
            }?.hostAddress ?: "..."


    val Context.currentSystemUser: String
        get() = try {
            @Suppress("DEPRECATION")
            Settings.Secure.getString(contentResolver, "user_name") ?: "Unknown user"
        } catch (e: Exception) {
            "System User"
        }

    val EmptyBitmap
        get() = createBitmap(1, 1)

    val isDesignMode
        @Composable
        get() = LocalInspectionMode.current

    inline fun <reified T> Context.intent(
        block: Intent.() -> Unit
    ): Intent = Intent(applicationContext, T::class.java).apply(block)

    fun postDelayed(
        timeout: Long,
        block: () -> Unit
    ) = Handler().postDelayed(block, timeout)

    @Composable
    fun Modifier.applyIf(
        condition: Boolean,
        other: Modifier.() -> Modifier
    ): Modifier = if (condition) this.then(other(this)) else this

    @Composable
    fun rememberAssetImage(
        name: String = "avatar1.png",
        context: Context = LocalContext.current
    ) = remember {
        context.assets.open(name).use { inputStream ->
            BitmapFactory.decodeStream(inputStream).asImageBitmap()
        }
    }

    fun ComponentActivity.enableEdgeToEdge(
        statusBarColor: ComposeColor = DarkMD5,
        navigationBarColor: ComposeColor = DarkMD5,
    ) = enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(statusBarColor.toColorLong().toInt()),
        navigationBarStyle = SystemBarStyle.dark(navigationBarColor.toColorLong().toInt())
    )

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    fun ComponentActivity.setFullScreen() {
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
    fun ComponentActivity.dismissKeyguard() {
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

    fun ComponentActivity.acquireWakeLock() = runCatching {
        dismissWakeLock()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        this.wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MotionLauncher::AlertWakeLock"
        )
        wakeLock?.acquire(5_000)
    }.onFailure { e ->
        e.printStackTrace()
    }

    fun ComponentActivity.dismissWakeLock() {
        runCatching {
            this.wakeLock?.release()
        }.onFailure { e ->
            e.printStackTrace()
        }
        this.wakeLock = null
    }

    fun ComponentActivity.hideSystemBars() = runCatching {
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

    fun ComponentActivity.bringToFront() {
        runCatching {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val myPackage = BuildConfig.APPLICATION_ID
            val task = activityManager.appTasks.firstOrNull { task ->
                task.taskInfo.baseActivity?.packageName == myPackage
            }
            task?.moveToFront()
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    fun Context.registerMotionDetector(
        motionReceiver: MotionBroadcastReceiver
    ) = runCatching {
        MotionDetectionService.start(this)
        ContextCompat.registerReceiver(
            this,
            motionReceiver,
            IntentFilter(IntentAction.MOTION_DETECTED.action),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }.onFailure { e -> e.printStackTrace() }

    fun Context.unregisterMotionDetector(
        motionReceiver: MotionBroadcastReceiver
    ) = runCatching {
        unregisterReceiver(motionReceiver)
        MotionDetectionService.stop(this)
    }.onFailure { e -> e.printStackTrace() }

    @androidx.annotation.OptIn(UnstableApi::class)
    @Composable
    fun rememberExoPlayer(
        videoUri: Uri = Uri.EMPTY,
        repeatMode: @Player.RepeatMode Int = Player.REPEAT_MODE_OFF,
        startTime: Long = 0L,
        startOnReady: Boolean = true,
        onVideoFinish: (ExoPlayer) -> Unit = { p -> p.pause() },
        onFrame: (ExoPlayer) -> Unit = {}
    ): ExoPlayer? {
        val context = LocalContext.current
        val currentOnVideoFinish = rememberUpdatedState(onVideoFinish)
        val currentOnFrame = rememberUpdatedState(onFrame)
        val exoPlayer = runCatching {
            ExoPlayer.Builder(context).build().apply {
                setVideoFrameMetadataListener { _, _, _, _ ->
                    currentOnFrame.value.invoke(this@apply)
                }
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoPlayer", "ExoPlayer error: ${error.message}", error)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                if (currentPosition < startTime) {
                                    seekTo(startTime)
                                }
                            }

                            Player.STATE_ENDED -> {
                                currentOnVideoFinish.value.invoke(this@apply)
                                currentOnFrame.value.invoke(this@apply)
                            }

                            else -> {
                                // no op
                            }
                        }
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        val isDiscontinuity = reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION
                        if (isDiscontinuity) {
                            currentOnVideoFinish.value.invoke(this@apply)
                            currentOnFrame.value.invoke(this@apply)
                        }
                    }
                })
                setMediaItem(MediaItem.fromUri(videoUri))
                this.repeatMode = repeatMode
                this.playWhenReady = startOnReady
                prepare()
            }
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()
        DisposableEffect(videoUri) {
            onDispose {
                exoPlayer?.stop()
                exoPlayer?.release()
            }
        }
        return exoPlayer
    }

    @SuppressLint("UseKtx")
    @Composable
    fun LaunchPermissions(
        onPermissionsResult: ((Map<String, Boolean>) -> Unit)? = null,
        onAllPermissionsGranted: (suspend () -> Unit)? = null,
    ) {
        val context = LocalContext.current
        val permissions = getPermissions()
        val lifecycleOwner = LocalLifecycleOwner.current
        val hasOverlayPermission = android.Manifest.permission.SYSTEM_ALERT_WINDOW in permissions
        val overlayGranted = remember { mutableStateOf(Settings.canDrawOverlays(context)) }
        val regularPermissions = remember(permissions) {
            permissions.filter { it != android.Manifest.permission.SYSTEM_ALERT_WINDOW }
        }
        val overlayLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            overlayGranted.value = Settings.canDrawOverlays(context)
        }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && hasOverlayPermission) {
                    overlayGranted.value = Settings.canDrawOverlays(context)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        val permissionsState = rememberPermissionsState(
            permissions = regularPermissions,
            onPermissionsResult = { results ->
                val allResults = if (hasOverlayPermission) {
                    results + (android.Manifest.permission.SYSTEM_ALERT_WINDOW to overlayGranted.value)
                } else {
                    results
                }
                onPermissionsResult?.invoke(allResults)
            },
            onAllPermissionsGranted = {
                if (!hasOverlayPermission || overlayGranted.value) {
                    onAllPermissionsGranted?.invoke()
                }
            },
        )
        LaunchedEffect(
            permissionsState,
            permissionsState.allPermissionsGranted,
            permissionsState.shouldShowRationale,
            overlayGranted.value
        ) {
            if (hasOverlayPermission && !overlayGranted.value) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                overlayLauncher.launch(intent)
            }
            if (regularPermissions.isNotEmpty()) {
                permissionsState.launchPermissionRequest()
            } else if (!hasOverlayPermission) {
                onAllPermissionsGranted?.invoke()
            }
        }
    }

    @Composable
    private fun getPermissions(): List<String> {
        val context = LocalContext.current
        return remember(context) {
            runCatching {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.PackageInfoFlags.of(
                            PackageManager.GET_PERMISSIONS.toLong()
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.GET_PERMISSIONS
                    )
                }
                packageInfo.requestedPermissions?.toList()
            }.getOrNull() ?: emptyList()
        }
    }

    internal val fakePermissionsState = object : PermissionsState {
        override val allPermissionsGranted: Boolean = false
        override val shouldShowRationale: Boolean = false
        override fun launchPermissionRequest() = Unit
    }

    operator fun PaddingValues.plus(other: PaddingValues) = object : PaddingValues {
        override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
            this@plus.calculateLeftPadding(layoutDirection) +
                    other.calculateLeftPadding(layoutDirection)

        override fun calculateTopPadding() =
            this@plus.calculateTopPadding() + other.calculateTopPadding()

        override fun calculateRightPadding(layoutDirection: LayoutDirection) =
            this@plus.calculateRightPadding(layoutDirection) +
                    other.calculateRightPadding(layoutDirection)

        override fun calculateBottomPadding() =
            this@plus.calculateBottomPadding() + other.calculateBottomPadding()
    }

    @Stable
    interface PermissionsState {
        val allPermissionsGranted: Boolean
        val shouldShowRationale: Boolean
        fun launchPermissionRequest()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun rememberPermissionsState(
        permissions: List<String> = getPermissions(),
        onPermissionsResult: ((Map<String, Boolean>) -> Unit)? = null,
        onAllPermissionsGranted: (suspend () -> Unit)? = null,
    ): PermissionsState = if (LocalInspectionMode.current) fakePermissionsState else {
        val permissionState = rememberMultiplePermissionsState(permissions) {
            onPermissionsResult?.invoke(it)
        }
        val allPermissionsGranted = permissionState.allPermissionsGranted
        LaunchedEffect(allPermissionsGranted) {
            if (allPermissionsGranted) {
                onAllPermissionsGranted?.invoke()
            }
        }
        remember(permissions) {
            object : PermissionsState {
                override val allPermissionsGranted: Boolean
                    get() = permissionState.allPermissionsGranted
                override val shouldShowRationale: Boolean
                    get() = permissionState.shouldShowRationale

                override fun launchPermissionRequest() {
                    permissionState.launchMultiplePermissionRequest()
                }
            }
        }
    }

    fun LifecycleOwner.launchOnLifecycle(
        scope: LifecycleCoroutineScope = lifecycleScope,
        context: CoroutineContext = Dispatchers.Main,
        block: suspend CoroutineScope.() -> Unit
    ) = scope.launch(
        context = context,
        block = block
    )

    @Composable
    fun rememberDeviceCapture(
        device: NsdDevice,
        lifecycleScope: LifecycleCoroutineScope
    ): MutableState<Bitmap?> = remember(device) {
        val image: MutableState<Bitmap?> = mutableStateOf(null)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                while (true) {
                    image.value = device.getFrame()
                    delay(10)
                }
            }
        }
        image
    }
}
