package org.mjdev.doorbellassistant.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.doorbellassistant.extensions.ComposeExt.acquireWakeLock
import org.mjdev.doorbellassistant.extensions.ComposeExt.dismissWakeLock
import org.mjdev.doorbellassistant.helpers.DelayHandler
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver.Companion.rememberMotionDetector
import org.mjdev.doorbellassistant.rpc.CaptureRoute.sendMotionDetected
import org.mjdev.doorbellassistant.rpc.CaptureRoute.sendMotionUnDetected
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.ui.screens.MainScreen
import org.mjdev.phone.activity.IntercomActivity
import org.mjdev.phone.activity.base.UnlockedActivity
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

class AssistantActivity : UnlockedActivity() {
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

    override fun onStart() {
        super.onStart()
        start<DoorbellNsdService>()
    }

    // todo
    override fun onResume() {
        super.onResume()
    }

    // todo
    override fun onPause() {
        super.onPause()
    }

    private fun resetToBaseState() {
        if (isMotionDetected.value) {
            isMotionDetected.value = false
        }
        delayHandler.stop()
        dismissWakeLock()
    }

    private fun handleMotionDetected() {
        if (isMotionDetected.value.not()) {
            isMotionDetected.value = true
        }
        if (isDoorBellAssistantRunning && !isRunning<IntercomActivity>()) {
            startOrResume<AssistantActivity>(this)
        }
        bringToFront()
        turnDisplayOn()
        dismissKeyguard()
        acquireWakeLock()
        nsdDevice { device ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    sendMotionDetected(device)
                }
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
                isAppSetAsHomeLauncher()
            }.getOrElse { false }

        fun Context.isAppSetAsHomeLauncher(): Boolean {
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
