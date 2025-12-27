package org.mjdev.doorbellassistant.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import org.mjdev.doorbellassistant.activity.AssistantActivity.Companion.isDoorBellAssistantRunning
import org.mjdev.doorbellassistant.extensions.ComposeExt.launchOnLifecycle
import org.mjdev.doorbellassistant.extensions.ComposeExt.rememberDeviceCapture
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdTypes
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdTypes.DOOR_BELL_CLIENT
import org.mjdev.doorbellassistant.helpers.nsd.rpc.INsdServerRPC
import org.mjdev.doorbellassistant.helpers.nsd.service.NsdService
import org.mjdev.doorbellassistant.rpc.DoorBellAction
import org.mjdev.doorbellassistant.rpc.DoorBellAssistantServerRpc
import org.mjdev.doorbellassistant.ui.components.FrontCameraPreview
import org.mjdev.doorbellassistant.ui.window.ComposeFloatingWindow
import org.mjdev.doorbellassistant.ui.window.ComposeFloatingWindow.Companion.alertLayoutParams

// todo automatic user login with wifi access
class DoorbellNsdService : NsdService() {
    override val port: Int = 8888

    override val serviceType: NsdTypes
        get() = if (baseContext.isDoorBellAssistantRunning) DOOR_BELL_ASSISTANT else DOOR_BELL_CLIENT

    override val rpcServer: INsdServerRPC by lazy {
        DoorBellAssistantServerRpc(
            context = baseContext,
            port = port,
            onAction = ::onRpcAction
        )
    }

    private fun showAlert(
        device: NsdDevice,
        context: Context = applicationContext,
    ) = launchOnLifecycle {
        ComposeFloatingWindow(
            context = context,
            windowParams = alertLayoutParams(context),
        ) {
            setContent {
                val imageState: MutableState<Bitmap?> = rememberDeviceCapture(
                    device,
                    lifecycleScope
                )
                FrontCameraPreview(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    imageState = imageState,
                    onClick = { hide() }
                )
            }
            device.serviceName?.let { name ->
                lastAlerts.filter { w ->
                    w.key == name
                }.map { d ->
                    d.value
                }.forEach { w ->
                    w.hide()
                }
                lastAlerts[name] = this
                show()
            }
        }
    }

    fun hideAlert(
        device: NsdDevice
    ) = launchOnLifecycle {
        device.serviceName?.let { name ->
            lastAlerts.filter { w ->
                w.key == name
            }.forEach { d ->
                lastAlerts.remove(d.key)
                d.value.hide()
            }
        }
    }

    override fun onCreate() {
        isRunning.value = true
        super.onCreate()
    }

    override fun onDestroy() {

        super.onDestroy()
        isRunning.value = false
    }

    fun onRpcAction(
        action: DoorBellAction
    ) {
        when (action) {
            is DoorBellAction.DoorBellActionMotionDetected -> {
                showAlert(action.device)
            }

            is DoorBellAction.DoorBellActionMotionUnDetected -> {
                hideAlert(action.device)
            }
        }
    }

    companion object {
        private val isRunning = mutableStateOf(false)
        private var lastAlerts = mutableMapOf<String, ComposeFloatingWindow>()

        fun start(
            context: Context
        ) = runCatching {
            if (isRunning.value.not()) Intent(
                context,
                DoorbellNsdService::class.java
            ).also { intent ->
                context.startForegroundService(intent)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }

        fun stop(
            context: Context
        ) = runCatching {
            if (isRunning.value) Intent(
                context,
                DoorbellNsdService::class.java
            ).also { intent ->
                context.stopService(intent)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
    }
}
