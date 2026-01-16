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
import org.mjdev.doorbellassistant.extensions.ComposeExt.registerMotionDetector
import org.mjdev.doorbellassistant.extensions.ComposeExt.unregisterMotionDetector
import org.mjdev.doorbellassistant.rpc.CaptureRoute.sendMotionDetected
import org.mjdev.doorbellassistant.rpc.CaptureRoute.sendMotionUnDetected
import org.mjdev.phone.extensions.CustomExtensions.startOrResume
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
            startOrResume<AssistantActivity>(context)
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
