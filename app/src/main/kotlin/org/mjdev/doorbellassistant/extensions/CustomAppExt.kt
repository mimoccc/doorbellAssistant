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
import androidx.compose.runtime.State
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import org.mjdev.doorbellassistant.enums.IntentAction
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver
import org.mjdev.doorbellassistant.rpc.CaptureRoute.getFrame
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.phone.extensions.CustomExt.EmptyBitmap
import org.mjdev.phone.extensions.StateExt.produceStateInLifeCycleRepeated
import org.mjdev.phone.nsd.device.NsdDevice

@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION", "unused")
object CustomAppExt {
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
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        setAudioAttributes(audioAttributes, true)
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
        device: NsdDevice? = null,
        delayTime: Long = 40L,
    ): State<Bitmap?> = produceStateInLifeCycleRepeated(
        EmptyBitmap,
        delayTime,
        device,
    ) {
        runCatching {
            device?.getFrame()
        }.onFailure { e ->
            Log.e("DeviceCapture", "Frame error", e)
        }.getOrNull() ?: EmptyBitmap
    }
}
