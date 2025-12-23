package org.mjdev.doorbellassistant.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import org.mjdev.doorbellassistant.activity.base.FullScreenActivity
import org.mjdev.doorbellassistant.extensions.ComposeExt.acquireWakeLock
import org.mjdev.doorbellassistant.extensions.ComposeExt.bringToFront
import org.mjdev.doorbellassistant.extensions.ComposeExt.dismissKeyguard
import org.mjdev.doorbellassistant.extensions.ComposeExt.dismissWakeLock
import org.mjdev.doorbellassistant.extensions.ComposeExt.turnDisplayOff
import org.mjdev.doorbellassistant.extensions.ComposeExt.turnDisplayOn
import org.mjdev.doorbellassistant.helpers.DelayHandler
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver.Companion.rememberMotionDetector
import org.mjdev.doorbellassistant.ui.screens.MainScreen

class AssistantActivity : FullScreenActivity() {
    companion object {
        val isMotionDetected: MutableState<Boolean> = mutableStateOf(false)
        val isFinished = mutableStateOf(true)

        fun startOrResume(context: Context) {
            isMotionDetected.value = true
            if (isFinished.value) {
                Intent(context, AssistantActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                }.apply {
                    context.startActivity(this)
                }
            }
        }
    }

    val delayHandler by lazy {
        DelayHandler(60000L) {
            handleMotionLost()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent @androidx.annotation.RequiresPermission(android.Manifest.permission.RECORD_AUDIO) {
            rememberMotionDetector(
                onNoMotionDetected = {
                    handleMotionLost()
                },
                onMotionDetected = {
                    handleMotionDetected()
                }
            )
            MainScreen(
                modifier = Modifier.fillMaxSize(),
                motionState = isMotionDetected,
                onStartClick = {
                    handleMotionDetected()
                },
                onDismiss = {
                    handleMotionLost()
                    delayHandler.stop()
                },
                onWelcomeVideoFinished = {
                    delayHandler.start()
                },
                onConversationContinued = {
                    delayHandler.restart()
                },
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (isMotionDetected.value.not()) {
            isFinished.value = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (isMotionDetected.value) {
            isFinished.value = false
        }
    }

    private fun handleMotionDetected() {
        if (isMotionDetected.value.not()) {
            isMotionDetected.value = true
        }
        startOrResume(this)
        bringToFront()
        turnDisplayOn()
        dismissKeyguard()
        acquireWakeLock()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        // disable finish
    }

    override fun finish() {
        // disable finish
    }

    private fun handleMotionLost() {
        if (isMotionDetected.value) {
            isMotionDetected.value = false
        }
        turnDisplayOff()
        dismissWakeLock()
    }
}
