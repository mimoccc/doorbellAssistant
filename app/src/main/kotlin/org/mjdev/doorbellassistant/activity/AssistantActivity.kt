package org.mjdev.doorbellassistant.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.extensions.ComposeExt.acquireWakeLock
import org.mjdev.doorbellassistant.extensions.ComposeExt.dismissWakeLock
import org.mjdev.doorbellassistant.helpers.DelayHandler
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver.Companion.rememberMotionDetector
import org.mjdev.doorbellassistant.rpc.CaptureRoute.sendMotionDetected
import org.mjdev.doorbellassistant.rpc.CaptureRoute.sendMotionUnDetected
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.ui.screens.MainScreen
import org.mjdev.phone.activity.base.FullScreenActivity
import org.mjdev.phone.extensions.CustomExtensions.ANDROID_ID
import org.mjdev.phone.extensions.CustomExtensions.bringToFront
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.extensions.CustomExtensions.dismissKeyguard
import org.mjdev.phone.extensions.CustomExtensions.isRunning
import org.mjdev.phone.extensions.CustomExtensions.startOrResume
import org.mjdev.phone.extensions.CustomExtensions.turnDisplayOff
import org.mjdev.phone.extensions.CustomExtensions.turnDisplayOn
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.service.CallNsdService.Companion.nsdDevice
import org.mjdev.phone.nsd.service.CallNsdService.Companion.start

class AssistantActivity : FullScreenActivity() {
    val delayHandler by lazy {
        DelayHandler(60000L) {
            handleMotionLost()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
        start<DoorbellNsdService>()
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
        nsdDevice<DoorbellNsdService> { device ->
            CoroutineScope(Dispatchers.IO).launch {
                sendMotionDetected(device)
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
                serviceType = NsdTypes.DOOR_BELL_ASSISTANT,
                serviceName = ANDROID_ID,
                port = DoorbellNsdService.nsdPort
            ).also { d ->
                sendMotionUnDetected(d)
            }
        }
    }

    companion object {
        val isMotionDetected: MutableState<Boolean> = mutableStateOf(false)

        val Context.isDoorBellAssistantRunning
            get() = runCatching {
                isRunning<AssistantActivity>()
            }.getOrElse { false }
    }
}
