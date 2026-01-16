package org.mjdev.doorbellassistant.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import org.mjdev.doorbellassistant.extensions.ComposeExt.acquireWakeLock
import org.mjdev.doorbellassistant.extensions.ComposeExt.dismissWakeLock
import org.mjdev.doorbellassistant.extensions.ComposeExt.unregisterMotionDetector
import org.mjdev.doorbellassistant.helpers.DelayHandler
import org.mjdev.doorbellassistant.helpers.MotionDetector.Companion.sendMotionIntent
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver.Companion.rememberMotionDetector
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.doorbellassistant.ui.screens.MainScreen
import org.mjdev.phone.activity.VideoCallActivity
import org.mjdev.phone.activity.base.UnlockedActivity
import org.mjdev.phone.extensions.CustomExtensions.bringToFront
import org.mjdev.phone.extensions.CustomExtensions.dismissKeyguard
import org.mjdev.phone.extensions.CustomExtensions.isRunning
import org.mjdev.phone.extensions.CustomExtensions.turnDisplayOff
import org.mjdev.phone.extensions.CustomExtensions.turnDisplayOn
import org.mjdev.phone.nsd.service.CallNsdService.Companion.start

class AssistantActivity : UnlockedActivity() {
    val delayHandler by lazy {
        DelayHandler(8000L) {
            // handleMotionLost(false)
        }
    }

    val isInCall
        get() = isRunning<VideoCallActivity>()

    private val isMotionDetected = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            rememberMotionDetector(
                onNoMotionDetected = {
                    isMotionDetected.value = false
                },
                onMotionDetected = {
                    handleMotionDetected(true)
                }
            )
            MainScreen(
                modifier = Modifier.fillMaxSize(),
                motionState = isMotionDetected,
                onStartClick = {
                    handleMotionDetected(false)
                },
                onDismiss = {
                    handleMotionLost(false)
                    delayHandler.stop()
                },
                onWelcomeVideoFinished = {
                    delayHandler.start()
                },
                onConversationContinued = {
                    delayHandler.restart()
                },
                onThinking = {
                    delayHandler.stop()
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        start<DoorbellNsdService>()
        isMotionDetected.value = false
    }

    override fun onStop() {
        super.onStop()
        if (!isDoorBellAssistantEnabled) {
            MotionDetectionService.stop(this)
        }
    }

    override fun onPause() {
        super.onPause()
        delayHandler.stop()
        dismissWakeLock()
    }

    private fun handleMotionDetected(fromIntent: Boolean) {
        if (!isInCall && !isMotionDetected.value) {
            isMotionDetected.value = true
            bringToFront()
            turnDisplayOn()
            dismissKeyguard()
            acquireWakeLock()
        }
        if (!fromIntent) {
            sendMotionIntent(true)
        }
    }

    private fun handleMotionLost(fromIntent: Boolean) {
        if (isMotionDetected.value) {
            isMotionDetected.value = false
            turnDisplayOff()
            dismissWakeLock()
        }
        if (!fromIntent) {
            sendMotionIntent(false)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (isAppSetAsHomeLauncher) {
            // disable finish
        } else {
            super.onBackPressed()
        }
    }

    override fun finish() {
        if (isAppSetAsHomeLauncher) {
            // disable finish
        } else {
            super.finish()
        }
    }

    companion object {
        val Context.isAssistantRunning
            get() = isRunning<AssistantActivity>()

        val Context.isDoorBellAssistantEnabled
            get() = isAppSetAsHomeLauncher || isAssistantRunning

        val Context.isAppSetAsHomeLauncher: Boolean
            get() {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                }
                val resolveInfo = packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                return resolveInfo?.activityInfo?.packageName == packageName
            }
    }
}
