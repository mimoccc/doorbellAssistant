package org.mjdev.doorbellassistant.extensions

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import org.mjdev.doorbellassistant.BuildConfig
import org.mjdev.doorbellassistant.R
import org.mjdev.doorbellassistant.enums.IntentAction
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.doorbellassistant.ui.theme.DarkMD5
import androidx.compose.ui.graphics.Color as ComposeColor

@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION", "unused")
object ComposeExt {

    private val TAG_WAKE_LOCK: Int = R.string.wake_lock

    var ComponentActivity.wakeLock: WakeLock?
        get() = runCatching {
            window.decorView.getTag(TAG_WAKE_LOCK) as? WakeLock
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()
        set(value) {
            window.decorView.setTag(TAG_WAKE_LOCK, value)
        }

    val EmptyBitmap
        get() = createBitmap(1, 1)

    val isDesignMode
        @Composable
        get() = LocalInspectionMode.current

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
    ) {
        MotionDetectionService.start(this)
        ContextCompat.registerReceiver(
            this,
            motionReceiver,
            IntentFilter(IntentAction.MOTION_DETECTED.action),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun Context.unregisterMotionDetector(
        motionReceiver: MotionBroadcastReceiver
    ) {
        unregisterReceiver(motionReceiver)
        MotionDetectionService.stop(this)
    }

    @OptIn(UnstableApi::class)
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
}
