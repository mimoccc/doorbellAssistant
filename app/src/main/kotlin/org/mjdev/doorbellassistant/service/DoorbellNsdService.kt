/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.service

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.activity.AssistantActivity.Companion.isDoorBellAssistantEnabled
import org.mjdev.doorbellassistant.extensions.CustomAppExt.rememberDeviceCapture
import org.mjdev.doorbellassistant.rpc.DoorBellActions.DoorBellActionMotionDetected
import org.mjdev.doorbellassistant.rpc.DoorBellActions.DoorBellActionMotionUnDetected
import org.mjdev.doorbellassistant.rpc.DoorBellAssistantServerRpc
import org.mjdev.doorbellassistant.ui.components.FrontCameraPreview
import org.mjdev.doorbellassistant.ui.window.ComposeFloatingWindow
import org.mjdev.doorbellassistant.ui.window.ComposeFloatingWindow.Companion.alertLayoutParams
import org.mjdev.phone.extensions.ContextExt.currentWifiIP
import org.mjdev.phone.helpers.DelayHandler
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdType.DOOR_BELL_ASSISTANT
import org.mjdev.phone.nsd.device.NsdType.DOOR_BELL_CLIENT
import org.mjdev.phone.nsd.service.CallNsdService
import org.mjdev.phone.rpc.action.NsdAction
import org.mjdev.phone.rpc.server.INsdServerRPC

// todo automatic user login with wifi access
class DoorbellNsdService : CallNsdService(
    serviceNsdType = DOOR_BELL_CLIENT
) {
    override val rpcServer: INsdServerRPC by lazy {
        DoorBellAssistantServerRpc(
            context = baseContext,
            onAction = ::onRpcAction
        )
    }

    override fun onStarted(
        address: String,
        port: Int
    ) {
        super.onStarted(address, port)
        nsdPort = port
    }

    private fun showAlert(
        device: NsdDevice?,
        context: Context = applicationContext,
        dismissDelay: Long = 10000
    ) = CoroutineScope(Dispatchers.Main).launch {
        ComposeFloatingWindow(
            context = context,
            windowParams = alertLayoutParams(context),
            onShown = {
                DelayHandler(dismissDelay) {
                    hide()
                }.start()
            }
        ) {
            setContent {
                val imageState = rememberDeviceCapture(device, lifecycleScope)
                FrontCameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    imageState = imageState,
                    onClick = { hide() }
                )
            }
            device?.serviceName?.let { name ->
                lastAlerts
                    .filter { w -> w.key == name }
                    .map { d -> d.value }
                    .forEach { w -> w.hide() }
                lastAlerts[name] = this
                show()
            }
        }
    }

    fun hideAlert(
        device: NsdDevice?
    ) = CoroutineScope(Dispatchers.Main).launch {
        device?.serviceName?.let { name ->
            lastAlerts.filter { w ->
                w.key == name
            }.forEach { d ->
                lastAlerts.remove(d.key)
                d.value.hide()
            }
        }
    }

    override fun onRpcAction(
        action: NsdAction
    ) {
        super.onRpcAction(action)
        when (action) {
            is DoorBellActionMotionDetected -> {
                if (action.device?.address != currentWifiIP) {
                    showAlert(action.device)
                }
            }

            is DoorBellActionMotionUnDetected -> {
                hideAlert(action.device)
            }
        }
    }

    override fun checkDeviceType() {
        if (isDoorBellAssistantEnabled) {
            setNsdDeviceType(DOOR_BELL_ASSISTANT)
        } else {
            setNsdDeviceType(DOOR_BELL_CLIENT)
        }
    }

    companion object {
        @Volatile
        var nsdPort: Int = 8888
            internal set

        private var lastAlerts = mutableMapOf<String, ComposeFloatingWindow>()
    }
}
