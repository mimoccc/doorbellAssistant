package org.mjdev.doorbellassistant.extensions

import android.content.Context
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.doorbellassistant.R
import org.mjdev.doorbellassistant.enums.IntentAction
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver
import org.mjdev.doorbellassistant.rpc.CaptureRoute.getFrame
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.nsd.device.NsdDevice

@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION")
object ComposeExt {

    private val TAG_WAKE_LOCK: Int = R.string.wake_lock

    fun String.toByteArray(): ByteArray = split(".").map {
        it.toInt().toByte()
    }.toByteArray()

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

    val EmptyBitmap
        get() = createBitmap(1, 1)

    fun postDelayed(
        timeout: Long,
        block: () -> Unit
    ) = Handler().postDelayed(block, timeout)

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
        if(isPreview) return null
        val context = LocalContext.current
        val currentOnVideoFinish = rememberUpdatedState(onVideoFinish)
        val currentOnFrame = rememberUpdatedState(onFrame)
        val exoPlayer = ExoPlayer.Builder(context).build().apply {
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
        DisposableEffect(videoUri) {
            onDispose {
                exoPlayer.stop()
                exoPlayer.release()
            }
        }
        return exoPlayer
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

    @Composable
    fun rememberDeviceCapture(
        device: NsdDevice?,
        lifecycleScope: LifecycleCoroutineScope?
    ): MutableState<Bitmap?> = remember(device) {
        val image: MutableState<Bitmap?> = mutableStateOf(null)
        lifecycleScope?.launch {
            withContext(Dispatchers.IO) {
                while (true) {
                    image.value = device?.getFrame()
                    delay(10)
                }
            }
        }
        image
    }

}
