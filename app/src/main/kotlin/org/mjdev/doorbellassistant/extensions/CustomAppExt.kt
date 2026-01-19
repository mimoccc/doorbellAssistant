/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.extensions

import android.content.Context
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
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
import org.mjdev.doorbellassistant.enums.IntentAction
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver
import org.mjdev.doorbellassistant.rpc.CaptureRoute.getFrame
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.phone.nsd.device.NsdDevice

@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION")
object CustomAppExt {

//    fun String.toByteArray(): ByteArray = split(".").map {
//        it.toInt().toByte()
//    }.toByteArray()

//    fun ImageVector.toDrawable(
//        context: Context,
//        width: Int,
//        height: Int
//    ): BitmapDrawable {
//        val computedWidth = if (width < 1) 1 else width
//        val computedHeight = if (height < 1) 1 else height
//        val bitmap = createBitmap(computedWidth, computedHeight)
//        val canvas = Canvas(bitmap)
//        val vectorDrawable = VectorDrawable()
//        vectorDrawable.setBounds(0, 0, computedWidth, computedHeight)
//        vectorDrawable.draw(canvas)
//        return BitmapDrawable(context.resources, bitmap)
//    }

//    fun ImageVector.toDrawable(
//        view: View,
//        width: Int = view.width,
//        height: Int = view.height
//    ): BitmapDrawable {
//        val computedWidth = if (width < 1) 1 else width
//        val computedHeight = if (height < 1) 1 else height
//        val bitmap = createBitmap(computedWidth, computedHeight)
//        val canvas = Canvas(bitmap)
//        val vectorDrawable = VectorDrawable()
//        vectorDrawable.setBounds(0, 0, computedWidth, computedHeight)
//        vectorDrawable.draw(canvas)
//        return BitmapDrawable(view.context.resources, bitmap)
//    }

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

    @Suppress("unused")
    fun Context.unregisterMotionDetector(
        motionReceiver: MotionBroadcastReceiver
    ) = runCatching {
        unregisterReceiver(motionReceiver)
        MotionDetectionService.stop(this)
    }.onFailure { e -> e.printStackTrace() }

    @OptIn(UnstableApi::class)
    fun createExoPlayer(
        context: Context,
        videoUri: Uri = Uri.EMPTY,
        repeatMode: @Player.RepeatMode Int = Player.REPEAT_MODE_OFF,
        startTime: Long = 0L,
        startOnReady: Boolean = true,
        onVideoFinish: (ExoPlayer) -> Unit = { p -> p.pause() },
        onFrame: (ExoPlayer) -> Unit = {},
        onVideoStart: (ExoPlayer) -> Unit = {},
    ): ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setVideoFrameMetadataListener { _, _, _, _ ->
            onFrame(this@apply)
        }
        addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("VideoPlayer", "ExoPlayer error: ${error.message}", error)
//                onVideoFinish(this@apply)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (currentPosition < startTime) {
                            seekTo(startTime)
                        }
                    }

                    Player.STATE_ENDED -> {
                        onVideoFinish(this@apply)
                        onFrame(this@apply)
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
                    onVideoFinish(this@apply)
                    onFrame(this@apply)
                }
            }
        })
        this.repeatMode = repeatMode
        this.playWhenReady = startOnReady
        if (videoUri != Uri.EMPTY) {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }

    @Suppress("ParamsComparedByRef")
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

    fun logPosition() {
        RuntimeException("POSITION").printStackTrace()
    }
}
