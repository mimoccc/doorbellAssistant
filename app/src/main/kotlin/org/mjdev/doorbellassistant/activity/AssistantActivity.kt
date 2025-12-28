package org.mjdev.doorbellassistant.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.activity.base.FullScreenActivity
import org.mjdev.doorbellassistant.extensions.ComposeExt.ANDROID_ID
import org.mjdev.doorbellassistant.extensions.ComposeExt.acquireWakeLock
import org.mjdev.doorbellassistant.extensions.ComposeExt.bringToFront
import org.mjdev.doorbellassistant.extensions.ComposeExt.currentWifiIP
import org.mjdev.doorbellassistant.extensions.ComposeExt.dismissKeyguard
import org.mjdev.doorbellassistant.extensions.ComposeExt.dismissWakeLock
import org.mjdev.doorbellassistant.extensions.ComposeExt.turnDisplayOff
import org.mjdev.doorbellassistant.extensions.ComposeExt.turnDisplayOn
import org.mjdev.doorbellassistant.helpers.DelayHandler
import org.mjdev.doorbellassistant.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.nsd.device.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver.Companion.rememberMotionDetector
import org.mjdev.doorbellassistant.rpc.DoorBellAssistantServerRpc.Companion.sendMotionDetected
import org.mjdev.doorbellassistant.rpc.DoorBellAssistantServerRpc.Companion.sendMotionUnDetected
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.ui.screens.MainScreen

class AssistantActivity : FullScreenActivity() {
    companion object {
        val isMotionDetected: MutableState<Boolean> = mutableStateOf(false)

        val Context.isDoorBellAssistantRunning
            get() = runCatching {
                isRunning<AssistantActivity>()
            }.getOrElse { false }
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

    override fun onResume() {
        super.onResume()
        DoorbellNsdService.start(this)
    }

    private fun handleMotionDetected() {
        if (isMotionDetected.value.not()) {
            isMotionDetected.value = true
        }
//        if (context.isAssistantEnabled) {
        startOrResume<AssistantActivity>(this)
//        }
        bringToFront()
        turnDisplayOn()
        dismissKeyguard()
        acquireWakeLock()
        CoroutineScope(Dispatchers.IO).launch {
            NsdDevice.fromData(
                address = currentWifiIP,
                serviceType = DOOR_BELL_ASSISTANT,
                serviceName = ANDROID_ID
            ).also { d ->
                sendMotionDetected(d)
            }
        }
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
        CoroutineScope(Dispatchers.IO).launch {
            NsdDevice.fromData(
                address = currentWifiIP,
                serviceType = DOOR_BELL_ASSISTANT,
                serviceName = ANDROID_ID
            ).also { d ->
                sendMotionUnDetected(d)
            }
        }
    }
}
