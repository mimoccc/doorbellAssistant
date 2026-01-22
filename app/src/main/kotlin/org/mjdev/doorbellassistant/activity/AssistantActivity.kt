/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import org.mjdev.doorbellassistant.helpers.MotionDetector.Companion.sendMotionIntent
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver.Companion.rememberMotionDetector
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.doorbellassistant.service.TTSService
import org.mjdev.doorbellassistant.ui.screens.MainScreen
import org.mjdev.phone.activity.VideoCallActivity
import org.mjdev.phone.activity.base.UnlockedActivity
import org.mjdev.phone.extensions.ActivityExt.bringToFront
import org.mjdev.phone.extensions.ActivityExt.isRunning
import org.mjdev.phone.extensions.ActivityExt.turnDisplayOff
import org.mjdev.phone.extensions.ActivityExt.turnDisplayOn
import org.mjdev.phone.extensions.ContextExt.startForeground
import org.mjdev.phone.extensions.ContextExt.startService
import org.mjdev.phone.extensions.KeyGuardExt.dismissKeyguard
import org.mjdev.phone.extensions.WakeLockExt.acquireWakeLock
import org.mjdev.phone.extensions.WakeLockExt.dismissWakeLock
import org.mjdev.phone.helpers.DelayHandler
import org.mjdev.phone.nsd.device.NsdType
import org.mjdev.phone.nsd.service.CallNsdService.Companion.setNsdDeviceType

class AssistantActivity : UnlockedActivity() {
    private val isMotionDetected = mutableStateOf(false)

    private val delayHandler by lazy {
        DelayHandler(DEFAULT_DELAY_MOTION_DETECTION) {
            handleMotionLost(false)
        }
    }

    private val isInCall: Boolean
        get() = isRunning<VideoCallActivity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNsdDeviceType(NsdType.DOOR_BELL_ASSISTANT)
//        startService<STTService>()
        startService<TTSService>()
//        startService<AIService>()
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
        startForeground<DoorbellNsdService>()
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
            bringToFront<AssistantActivity>()
            turnDisplayOn()
            dismissKeyguard()
            acquireWakeLock()
        }
        if (!fromIntent) {
            sendMotionIntent(true)
        }
    }

    @Suppress("SameParameterValue")
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
        const val DEFAULT_DELAY_MOTION_DETECTION = 10000L

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
