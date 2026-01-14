package org.mjdev.doorbellassistant.service

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.activity.AssistantActivity.Companion.isDoorBellAssistantEnabled
import org.mjdev.doorbellassistant.extensions.ComposeExt.rememberDeviceCapture
import org.mjdev.doorbellassistant.rpc.DoorBellActions
import org.mjdev.doorbellassistant.rpc.DoorBellAssistantServerRpc
import org.mjdev.doorbellassistant.ui.components.FrontCameraPreview
import org.mjdev.doorbellassistant.ui.window.ComposeFloatingWindow
import org.mjdev.doorbellassistant.ui.window.ComposeFloatingWindow.Companion.alertLayoutParams
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.rpc.server.INsdServerRPC
import org.mjdev.phone.rpc.action.NsdAction
import org.mjdev.phone.nsd.service.CallNsdService

// todo automatic user login with wifi access
class DoorbellNsdService : CallNsdService() {
    override val serviceType: NsdTypes
        get() = if (baseContext.isDoorBellAssistantEnabled)
            NsdTypes.DOOR_BELL_ASSISTANT
        else
            NsdTypes.DOOR_BELL_CLIENT

    override val rpcServer: INsdServerRPC by lazy {
        DoorBellAssistantServerRpc(
            context = baseContext,
            onAction = ::onRpcAction
        )
    }

    override fun onStarted(address: String, port: Int) {
        super.onStarted(address, port)
        nsdPort = port
    }

    private fun showAlert(
        device: NsdDevice?,
        context: Context = applicationContext,
    ) = CoroutineScope(Dispatchers.Main).launch {
        ComposeFloatingWindow(
            context = context,
            windowParams = alertLayoutParams(context),
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
            is DoorBellActions.DoorBellActionMotionDetected -> {
                if (action.device?.address != currentWifiIP) {
                    showAlert(action.device)
                }
            }

            is DoorBellActions.DoorBellActionMotionUnDetected -> {
                hideAlert(action.device)
            }
        }
    }

    companion object {
        @Volatile
        var nsdPort: Int = 8888
            internal set

        private var lastAlerts = mutableMapOf<String, ComposeFloatingWindow>()
    }
}
