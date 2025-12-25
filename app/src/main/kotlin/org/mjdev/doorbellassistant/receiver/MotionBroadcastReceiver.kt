package org.mjdev.doorbellassistant.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import org.mjdev.doorbellassistant.activity.AssistantActivity
import org.mjdev.doorbellassistant.activity.base.BaseActivity
import org.mjdev.doorbellassistant.activity.base.BaseActivity.Companion.startOrResume
import org.mjdev.doorbellassistant.enums.IntentAction
import org.mjdev.doorbellassistant.extensions.ComposeExt.registerMotionDetector

class MotionBroadcastReceiver : BroadcastReceiver() {
    fun addListener(value: (Context) -> Unit) {
        receivers.add(value)
    }

    fun removeListener(value: (Context) -> Unit) {
        receivers.remove(value)
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val isMotionEvent = intent.action == IntentAction.MOTION_DETECTED.action
        val isNotActive = AssistantActivity.isMotionDetected.value.not()
        if (isMotionEvent && isNotActive) {
            startOrResume<AssistantActivity>(context)
            receivers.forEach { receiver ->
                receiver.invoke(context)
            }
        }
    }

    companion object {
        private val receivers: MutableList<(Context) -> Unit> = mutableListOf()

        @SuppressLint("ComposableNaming")
        @Composable
        fun rememberMotionDetector(
            context: Context = LocalContext.current,
            onNoMotionDetected: (Context) -> Unit = {},
            onMotionDetected: (Context) -> Unit
        ) = DisposableEffect(context, onNoMotionDetected, onMotionDetected) {
            val receiver = MotionBroadcastReceiver().apply {
                addListener(onMotionDetected)
            }
            context.registerMotionDetector(receiver)
            onDispose {
                receiver.removeListener(onMotionDetected)
                context.unregisterReceiver(receiver)
            }
        }
    }
}
