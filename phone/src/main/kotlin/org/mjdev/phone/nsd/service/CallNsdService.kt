/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.nsd.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.annotation.CallSuper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.mjdev.phone.activity.VideoCallActivity.Companion.startCall
import org.mjdev.phone.application.CallApplication.Companion.getCallServiceClass
import org.mjdev.phone.extensions.ContextExt.ANDROID_ID
import org.mjdev.phone.extensions.ContextExt.currentWifiIP
import org.mjdev.phone.helpers.DelayHandler
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdDevice.Companion.EMPTY
import org.mjdev.phone.nsd.device.NsdDevice.Companion.fromData
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.service.NsdService.Companion.NsdDeviceEvent
import org.mjdev.phone.nsd.service.NsdService.Companion.NsdDevicesEvent
import org.mjdev.phone.nsd.service.NsdService.Companion.NsdStateEvent
import org.mjdev.phone.rpc.action.NsdAction
import org.mjdev.phone.rpc.action.NsdActions
import org.mjdev.phone.rpc.action.NsdActions.SDPAccept
import org.mjdev.phone.rpc.action.NsdActions.SDPAnswer
import org.mjdev.phone.rpc.action.NsdActions.SDPDismiss
import org.mjdev.phone.rpc.action.NsdActions.SDPGetState
import org.mjdev.phone.rpc.action.NsdActions.SDPIceCandidate
import org.mjdev.phone.rpc.action.NsdActions.SDPOffer
import org.mjdev.phone.rpc.action.NsdActions.SDPStartCall
import org.mjdev.phone.rpc.action.NsdActions.SDPStartCallStarted
import org.mjdev.phone.rpc.server.INsdServerRPC
import org.mjdev.phone.rpc.server.NsdServerRpc
import org.mjdev.phone.rpc.server.NsdServerRpc.Companion.sendAction
import org.mjdev.phone.service.ServiceCommand
import org.mjdev.phone.service.ServiceCommand.Companion.GetNsdDevice
import org.mjdev.phone.service.ServiceCommand.Companion.GetNsdDevices
import org.mjdev.phone.service.ServiceCommand.Companion.GetState
import org.mjdev.phone.service.ServiceEvent
import org.mjdev.phone.service.ServiceEvent.Companion.ServiceConnected
import org.mjdev.phone.service.ServiceEvent.Companion.ServiceDisconnected
import org.mjdev.phone.service.ServiceEvent.Companion.ServiceError
import org.mjdev.phone.stream.CallEndReason

// todo automatic user login with wifi access
@Suppress("unused")
abstract class CallNsdService(
    val serviceNsdType: NsdTypes,
    val deviceTypeCheckDelay: Long = 1000L
) : NsdService(serviceNsdType) {
    private val delayedHandler by lazy {
        DelayHandler(
            timeout = deviceTypeCheckDelay,
            repeated = true
        ) {
            checkDeviceType()
        }
    }
    override val rpcServer: INsdServerRPC by lazy {
        NsdServerRpc(
            context = baseContext,
            onAction = ::onRpcAction,
            onStarted = { a, p ->
                onStarted(a, p)
            },
            onStopped = {
                onStopped()
            },
            additionalRoutes = {}
        )
    }

    override fun onCreate() {
        isRunning.value = true
        super.onCreate()
        delayedHandler.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        sendEvent(ServiceDisconnected)
        isRunning.value = false
    }

    open fun checkDeviceType() {
        setNsdDeviceType(serviceNsdType)
    }

    @CallSuper
    override fun executeCommand(
        command: ServiceCommand,
        handler: (ServiceEvent) -> Unit
    ) {
        when (command) {
            is GetState -> {
                if (rpcServer.isRunning) {
                    handler(NsdStateEvent(currentWifiIP, rpcServer.port))
                } else {
                    handler(ServiceDisconnected)
                }
            }

            is GetNsdDevice -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    val nsdDevice: NsdDevice = if (rpcServer.isRunning) {
                        fromData(
                            address = rpcServer.address,
                            serviceName = ANDROID_ID,
                            serviceType = serviceType,
                            port = rpcServer.port
                        )
                    } else EMPTY
                    handler(NsdDeviceEvent(nsdDevice))
                }
            }

            is GetNsdDevices -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    val types = command.types.let { tt ->
                        if (tt.isNullOrEmpty()) NsdTypes.entries else tt
                    }
                    val filteredDevices = devicesAround.first().filter { d ->
                        d.serviceType in types
                    }
                    handler(NsdDevicesEvent(filteredDevices))
                }
            }

            else -> {
                super.executeCommand(command, handler)
            }
        }
    }

    @CallSuper
    open fun onStarted(
        address: String,
        port: Int
    ) {
        sendEvent(NsdStateEvent(address = address, port = port))
    }

    @CallSuper
    open fun onStopped() {
        sendEvent(ServiceDisconnected)
    }

    @CallSuper
    open fun onRpcAction(action: NsdAction) {
        when (action) {
            is SDPGetState -> {
                handleStateRequest(action)
            }

            is SDPStartCall -> {
                Log.d(
                    TAG,
                    "Received SDPStartCall: caller=${action.caller}, callee=${action.callee}"
                )
                handleCallReceived(action)
            }

            is SDPStartCallStarted -> {
                Log.d(
                    TAG,
                    "Received SDPStartCallStarted: caller=${action.caller}, callee=${action.callee}"
                )
                handleCallStarted(action)
            }

            is SDPIceCandidate -> {
                Log.d(
                    TAG,
                    "Received SDPIceCandidate: sdpMid=${action.sdpMid}, sdpMLineIndex=${action.sdpMLineIndex}, sdp=${action.sdp}"
                )
                handleIceCandidate(action)
            }

            is SDPDismiss -> {
                Log.d(TAG, "Received SDPDismiss")
                handleCallDismiss(action)
            }

            is SDPAccept -> {
                Log.d(TAG, "Received SDPAccept: sdp=${action.sdp}")
                handleCallAccept(action)
            }

            is SDPAnswer -> {
                Log.d(TAG, "Received SDPAnswer: sdp=${action.sdp}")
                handleCallAnswer(action)
            }

            is SDPOffer -> {
                Log.d(TAG, "Received SDPOffer: sdp=${action.sdp}")
                handleCallOffer(action)
            }

            else -> {
                Log.w(TAG, "Unhandled action : $action")
            }
        }
    }

    private fun handleStateRequest(message: SDPGetState) {
        checkDeviceType()
        nsdDevice { result ->
            message.sender?.sendAction(NsdActions.SDPState(result))
        }
    }

    private fun handleCallReceived(message: SDPStartCall) {
        Log.d(
            TAG, "Starting VideoCallActivity for caller=${
                message.caller
            }, callee=${
                message.callee
            }"
        )
        startCall(
            this::class.java,
            callee = message.callee,
            caller = message.caller
        )
    }

    private fun handleCallStarted(message: SDPStartCallStarted) {
        Log.d(TAG, "handleCallStarted: Notifying CallManagers about started call.")
        rpcServer.getCallManagers().forEach { cm ->
            cm.handleCallStarted(message.caller, message.callee)
        }
    }

    private fun handleIceCandidate(candidate: SDPIceCandidate) {
        Log.d(TAG, "handleIceCandidate: Notifying CallManagers about ICE candidate.")
        rpcServer.getCallManagers().forEach { cm ->
            cm.handleIceCandidate(candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp)
        }
    }

    private fun handleCallDismiss(action: SDPDismiss) {
        Log.d(TAG, "handleCallDismiss: Notifying CallManagers about call dismiss.")
        rpcServer.getCallManagers().forEach { cm ->
            cm.handleDismissReceived(CallEndReason.REMOTE_PARTY_END)
        }
    }

    private fun handleCallAccept(action: SDPAccept) {
        Log.d(TAG, "handleCallAccept: Notifying CallManagers about call accept.")
        rpcServer.getCallManagers().forEach { cm ->
            cm.handleAcceptReceived(action.sdp)
        }
    }

    private fun handleCallAnswer(action: SDPAnswer) {
        Log.d(TAG, "handleCallAnswer: Notifying CallManagers about SDP answer.")
        rpcServer.getCallManagers().forEach { cm ->
            cm.handleAnswerReceived(action.sdp)
        }
    }

    private fun handleCallOffer(action: SDPOffer) {
        Log.d(TAG, "handleCallOffer: Notifying CallManagers about SDP offer.")
        rpcServer.getCallManagers().forEach { cm ->
            cm.handleOfferReceived(action.sdp)
        }
    }

    companion object {
        private val TAG = CallNsdService::class.simpleName

        val isRunning = mutableStateOf(false)

        fun Context.setNsdDeviceType(
            type: NsdTypes
        ) {
            val serviceClass: Class<NsdService> = getCallServiceClass()
            val connection = object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName,
                    binder: IBinder
                ) {
                    val service = (binder as LocalBinder).service
                    (service as? NsdService)?.changeType(type) { old, new ->
                        unbindService(this)
                    }
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

        fun Context.nsdDevice(
            handler: (NsdDevice?) -> Unit
        ) {
            val serviceClass: Class<NsdService> = getCallServiceClass()
            val connection = object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName,
                    binder: IBinder
                ) {
                    val service = (binder as LocalBinder).service
                    service.executeCommand(GetNsdDevice) { event ->
                        handler((event as? NsdDeviceEvent)?.device)
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

        fun Context.nsdDevices(
            types: List<NsdTypes> = NsdTypes.entries,
            handler: (List<NsdDevice>) -> Unit
        ) {
            val serviceClass: Class<NsdService> = getCallServiceClass()
            val connection = object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName,
                    binder: IBinder
                ) {
                    val service = (binder as LocalBinder).service
                    service.executeCommand(
                        GetNsdDevices(types)
                    ) { event ->
                        handler(((event as? NsdDevicesEvent)?.devicesAround) ?: emptyList())
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
        fun rememberCallNsdService(
            context: Context = LocalContext.current
        ): Flow<CallNsdService?> = remember {
            callbackFlow {
                val serviceClass: Class<NsdService> = context.getCallServiceClass()
                val connection = object : ServiceConnection {
                    override fun onServiceConnected(
                        name: ComponentName,
                        binder: IBinder
                    ) {
                        trySend((binder as LocalBinder).service as CallNsdService?)
                    }

                    override fun onServiceDisconnected(name: ComponentName) {
                    }
                }
                context.bindService(
                    Intent(context, serviceClass),
                    connection,
                    BIND_AUTO_CREATE
                )
                awaitClose {
                    context.unbindService(connection)
                }
            }
        }

        @Composable
        fun ServiceState() {
            val context = LocalContext.current
            val event by context.bindableServiceFlow().collectAsState(initial = null)
            when (event) {
                is ServiceConnected -> {
                    val address = (event as NsdStateEvent).address
                    val port = (event as NsdStateEvent).port
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