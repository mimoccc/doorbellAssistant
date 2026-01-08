package org.mjdev.phone.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import org.mjdev.phone.activity.VideoCallActivity.Companion.startCall
import org.mjdev.phone.extensions.CustomExtensions.ANDROID_ID
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdDevice.Companion.fromData
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.rpc.INsdServerRPC
import org.mjdev.phone.nsd.service.NsdService
import org.mjdev.phone.rpc.NsdAction
import org.mjdev.phone.rpc.NsdActions.NsdActionCall
import org.mjdev.phone.rpc.NsdServerRpc
import kotlin.jvm.java

// todo automatic user login with wifi access
@Suppress("unused")
open class CallNsdService : NsdService() {

    data class ServiceConnected(
        val address: String,
        val port: Int
    ) : ServiceEvent()

    data class ServiceNsdDevice(
        val device: NsdDevice
    ) : ServiceEvent()

    object GetNsdDevice : ServiceCommand()

    override val serviceType: NsdTypes
        get() = NsdTypes.UNSPECIFIED

    override val rpcServer: INsdServerRPC by lazy {
        NsdServerRpc(
            context = baseContext,
            onAction = ::onRpcAction,
            onStarted = { a, p ->
                onStarted(a, p)
            },
            onStopped = {
                onStopped()
            }
        )
    }

    override fun onCreate() {
        isRunning.value = true
        super.onCreate()
    }

    @CallSuper
    open fun onRpcAction(action: NsdAction) {
        when (action) {
            is NsdActionCall -> {
                startCall(
                    this::class.java,
                    // todo devices non null
                    callee = null, //action.callee, // todo
                    caller = action.caller
                )
            }

            else -> Unit
        }
    }

    @CallSuper
    override fun executeCommand(command: ServiceCommand, handler: (ServiceEvent) -> Unit) {
        when (command) {
            is GetState -> {
                if (rpcServer.isRunning) {
                    handler(ServiceConnected(currentWifiIP, rpcServer.port))
                } else {
                    handler(ServiceDisconnected)
                }
            }

            is GetNsdDevice -> {
                val nsdDevice: NsdDevice = if (rpcServer.isRunning) fromData(
                    address = rpcServer.address,
                    serviceName = ANDROID_ID,
                    serviceType = serviceType,
                    port = rpcServer.port
                ) else fromData()
                handler(ServiceNsdDevice(nsdDevice))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sendEvent(ServiceDisconnected)
        isRunning.value = false
    }

    @CallSuper
    open fun onStarted(address: String, port: Int) {
        sendEvent(ServiceConnected(address = address, port = port))
    }

    open fun onStopped() {
        sendEvent(ServiceDisconnected)
    }

    companion object {
        val isRunning = mutableStateOf(false)

        inline fun <reified T : CallNsdService> Context.start() = runCatching {
            if (isRunning.value.not()) Intent(
                this,
                T::class.java
            ).also { intent ->
                startForegroundService(intent)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }

        inline fun <reified T : CallNsdService> Context.stop() = runCatching {
            if (isRunning.value) Intent(
                this,
                T::class.java
            ).also { intent ->
                stopService(intent)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }

        inline fun <reified T : BindableService> Context.nsdDevice(
            crossinline handler: (NsdDevice?) -> Unit
        ) {
            val command: ServiceCommand = GetNsdDevice
            val connection = object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName,
                    binder: IBinder
                ) {
                    val service = (binder as LocalBinder).service
                    service.executeCommand(command) { event ->
                        handler((event as? ServiceNsdDevice)?.device)
                    }
                    unbindService(this)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                }
            }
            bindService(
                Intent(this, T::class.java), connection, BIND_AUTO_CREATE
            )
        }

        fun Context.nsdDevice(
            serviceClass: Class<out NsdService>,
            handler: (NsdDevice?) -> Unit
        ) {
            val command: ServiceCommand = GetNsdDevice
            val connection = object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName,
                    binder: IBinder
                ) {
                    val service = (binder as LocalBinder).service
                    service.executeCommand(command) { event ->
                        handler((event as? ServiceNsdDevice)?.device)
                    }
                    unbindService(this)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                }
            }
            bindService(
                Intent(this, serviceClass),
                connection,
                BIND_AUTO_CREATE
            )
        }

        @Composable
        fun ServiceState() {
            val context = LocalContext.current
            val event by context.bindableServiceFlow().collectAsState(initial = null)
            when (event) {
                is ServiceConnected -> {
                    val address = (event as ServiceConnected).address
                    val port = (event as ServiceConnected).port
                    Text("Connected: $address:$port")
                }

                is ServiceError -> {
                    val error = (event as ServiceError).error
                    Text("Error: ${error.message}")
                }

                ServiceDisconnected -> {
                    Text("Disconnected")
                }

                null -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}