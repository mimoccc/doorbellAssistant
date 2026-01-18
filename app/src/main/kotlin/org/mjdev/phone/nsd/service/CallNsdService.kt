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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.mjdev.phone.activity.VideoCallActivity.Companion.startCall
import org.mjdev.phone.application.CallApplication.Companion.getCallServiceClass
import org.mjdev.phone.extensions.CustomExtensions.ANDROID_ID
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdDevice.Companion.EMPTY
import org.mjdev.phone.nsd.device.NsdDevice.Companion.fromData
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.rpc.server.INsdServerRPC
import org.mjdev.phone.rpc.action.NsdAction
import org.mjdev.phone.rpc.action.NsdActions.SDPStartCall
import org.mjdev.phone.rpc.action.NsdActions.SDPStartCallStarted
import org.mjdev.phone.rpc.action.NsdActions.SDPIceCandidate
import org.mjdev.phone.rpc.action.NsdActions.SDPDismiss
import org.mjdev.phone.rpc.action.NsdActions.SDPAccept
import org.mjdev.phone.rpc.action.NsdActions.SDPAnswer
import org.mjdev.phone.rpc.action.NsdActions.SDPOffer
import org.mjdev.phone.rpc.server.NsdServerRpc
import org.mjdev.phone.nsd.service.ServiceCommand.Companion.GetNsdDevice
import org.mjdev.phone.nsd.service.ServiceCommand.Companion.GetNsdDevices
import org.mjdev.phone.nsd.service.ServiceCommand.Companion.GetState
import org.mjdev.phone.nsd.service.ServiceEvent.Companion.ServiceNsdDevice
import org.mjdev.phone.nsd.service.ServiceEvent.Companion.ServiceNsdDevices
import org.mjdev.phone.stream.CallEndReason
import kotlin.jvm.java

// todo automatic user login with wifi access
@Suppress("unused")
abstract class CallNsdService : NsdService() {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        sendEvent(ServiceEvent.Companion.ServiceDisconnected)
        isRunning.value = false
    }

    @CallSuper
    override fun executeCommand(
        command: ServiceCommand,
        handler: (ServiceEvent) -> Unit
    ) {
        when (command) {
            is GetState -> {
                if (rpcServer.isRunning) {
                    handler(ServiceEvent.Companion.ServiceConnected(currentWifiIP, rpcServer.port))
                } else {
                    handler(ServiceEvent.Companion.ServiceDisconnected)
                }
            }

            is GetNsdDevice -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val nsdDevice: NsdDevice = if (rpcServer.isRunning) {
                        fromData(
                            address = rpcServer.address,
                            serviceName = ANDROID_ID,
                            serviceType = serviceType,
                            port = rpcServer.port
                        )
                    } else {
                        EMPTY
                    }
                    handler(ServiceNsdDevice(nsdDevice))
                }
            }

            is GetNsdDevices -> {
                val types = command.types.let { tt ->
                    if (tt == null || tt.isEmpty()) NsdTypes.entries else tt
                }
                val filteredDevices = devicesAround.filter { d ->
                    d.serviceType in types
                }
                CoroutineScope(Dispatchers.IO).launch {
                    handler(ServiceNsdDevices(filteredDevices))
                }
            }
        }
    }

    @CallSuper
    open fun onStarted(address: String, port: Int) {
        sendEvent(ServiceEvent.Companion.ServiceConnected(address = address, port = port))
    }

    @CallSuper
    open fun onStopped() {
        sendEvent(ServiceEvent.Companion.ServiceDisconnected)
    }

    @CallSuper
    open fun onRpcAction(action: NsdAction) {
        when (action) {
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

    private fun handleCallReceived(message: SDPStartCall) {
        Log.d(
            TAG,
            "handleCallReceived: Starting VideoCallActivity for caller=${message.caller}, callee=${message.callee}"
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
                        GetNsdDevices(
                            types
                        )
                    ) { event ->
                        handler(((event as? ServiceNsdDevices)?.devicesAround) ?: emptyList())
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
                is ServiceEvent.Companion.ServiceConnected -> {
                    val address = (event as ServiceEvent.Companion.ServiceConnected).address
                    val port = (event as ServiceEvent.Companion.ServiceConnected).port
                    Text("Connected: $address:$port")
                }

                is ServiceEvent.Companion.ServiceError -> {
                    val error = (event as ServiceEvent.Companion.ServiceError).error
                    Text("Error: ${error.message}")
                }

                ServiceEvent.Companion.ServiceDisconnected -> {
                    Text("Disconnected")
                }

                null -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}