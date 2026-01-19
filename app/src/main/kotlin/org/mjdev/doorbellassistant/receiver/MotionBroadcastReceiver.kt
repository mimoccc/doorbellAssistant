/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.activity.AssistantActivity
import org.mjdev.doorbellassistant.activity.AssistantActivity.Companion.isDoorBellAssistantEnabled
import org.mjdev.doorbellassistant.enums.IntentAction
import org.mjdev.doorbellassistant.extensions.CustomAppExt.registerMotionDetector
import org.mjdev.doorbellassistant.extensions.CustomAppExt.unregisterMotionDetector
import org.mjdev.doorbellassistant.rpc.CaptureRoute.sendMotionDetected
import org.mjdev.doorbellassistant.rpc.CaptureRoute.sendMotionUnDetected
import org.mjdev.phone.extensions.ActivityExt.startOrResume
import org.mjdev.phone.nsd.service.CallNsdService.Companion.nsdDevice

class MotionBroadcastReceiver : BroadcastReceiver() {
    fun addListener(value: (Boolean) -> Unit) {
        receivers.add(value)
    }

    fun removeListener(value: (Boolean) -> Unit) {
        receivers.remove(value)
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val isMotionDetected = intent.action == IntentAction.MOTION_DETECTED.action
        val isAssistantEnabled = context.isDoorBellAssistantEnabled
        if (isAssistantEnabled) {
            context.startOrResume<AssistantActivity>()
        }
        receivers.forEach { receiver ->
            receiver.invoke(isMotionDetected)
        }
        with(context.applicationContext) {
            nsdDevice { device ->
                CoroutineScope(Dispatchers.Default).launch {
                    if (isMotionDetected) {
                        sendMotionDetected(device)
                    } else {
                        sendMotionUnDetected(device)
                    }
                }
            }
        }
    }

    companion object {
        private val receivers: MutableList<(Boolean) -> Unit> = mutableListOf()

        @SuppressLint("ComposableNaming")
        @Composable
        fun rememberMotionDetector(
            onNoMotionDetected: () -> Unit = {},
            onMotionDetected: () -> Unit
        ) {
            val context: Context = LocalContext.current
            val motionListener: (Boolean) -> Unit = { isMotionDetected ->
                if (isMotionDetected) {
                    onMotionDetected()
                } else {
                    onNoMotionDetected()
                }
            }
            DisposableEffect(
                context,
                motionListener
            ) {
                val receiver = MotionBroadcastReceiver().apply {
                    addListener(motionListener)
                }
                context.registerMotionDetector(receiver)
                onDispose {
                    receiver.removeListener(motionListener)
                    context.unregisterMotionDetector(receiver)
                }
            }
        }
    }
}
