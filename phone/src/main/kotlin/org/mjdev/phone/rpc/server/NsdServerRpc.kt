/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.rpc.server

import android.content.Context
import android.util.Log
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.mjdev.phone.extensions.ContextExt.currentWifiIP
import org.mjdev.phone.helpers.json.ToolsJson.asJson
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdType
import org.mjdev.phone.nsd.service.CallNsdService.Companion.nsdDevices
import org.mjdev.phone.rpc.action.NsdAction
import org.mjdev.phone.rpc.action.NsdActions.SDPAccept
import org.mjdev.phone.rpc.action.NsdActions.SDPAnswer
import org.mjdev.phone.rpc.action.NsdActions.SDPDismiss
import org.mjdev.phone.rpc.action.NsdActions.SDPIceCandidate
import org.mjdev.phone.rpc.action.NsdActions.SDPOffer
import org.mjdev.phone.rpc.action.NsdActions.SDPStartCall
import org.mjdev.phone.rpc.action.NsdActions.SDPStartCallStarted
import org.mjdev.phone.rpc.plugins.StartupPlugin.StartupPlugin
import org.mjdev.phone.rpc.plugins.StopingPlugin.StopingPlugin
import org.mjdev.phone.rpc.routing.NsdRouting
import org.mjdev.phone.rpc.routing.NsdRoutingContext
import org.mjdev.phone.stream.ICallManager
import org.webrtc.IceCandidate

@OptIn(
    ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class,
    InternalSerializationApi::class
)
open class NsdServerRpc(
    val context: Context,
    var onStarted: (String, Int) -> Unit = { _, _ -> },
    var onStopped: () -> Unit = {},
    val onAction: (NsdAction) -> Unit = {},
    val additionalRoutes: NsdRouting.() -> Unit = {},
) : INsdServerRPC() {
    private val callManagers = mutableListOf<ICallManager>()

    override val address: String
        get() = context.currentWifiIP

    override var port: Int = 8888
        internal set

    override var isRunning: Boolean = false
        internal set

    protected val server by lazy {
        embeddedServer(factory = CIO, port = 0) {
            install(StartupPlugin) {
                onStart = { a, p ->
                    port = p
                    isRunning = true
                    onStarted(a, p)
                }
            }
            install(StopingPlugin) {
                onStop = {
                    isRunning = false
                    port = 8888
                    onStopped()
                }
            }
//            install(PeerSignalingRegistryPlugin) {
//                peerId = context.ANDROID_ID
//                service = P2PService()
//            }
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                    disableHtmlEscaping()
                }
            }
            routing {
                NsdRoutingContext(
                    this,
                    this@NsdServerRpc,
                    onAction
                ).apply {
                    actionSDPAccept()
                    actionSDPAnswer()
                    actionSDPDismiss()
                    actionSDPIceCandidate()
                    actionSDPOffer()
                    actionSDPOffer()
                    actionSDPStartCall()
                    actionSDPStartCallStarted()
                    additionalRoutes()
                }
            }
        }
    }

    override suspend fun start(
        onStarted: (String, Int) -> Unit
    ) {
        this.onStarted = onStarted
        server.start(wait = false)
    }

    override suspend fun stop(
        onStopped: () -> Unit
    ) {
        this.onStopped = onStopped
        server.stop(1000, 2000)
    }

    override fun sendIceCandidate(
        device: NsdDevice,
        candidate: IceCandidate
    ) {
        device.sendAction(SDPIceCandidate(device, candidate))
    }

    override fun sendAccept(
        device: NsdDevice,
        sdp: String
    ) {
        device.sendAction(SDPAccept(device, sdp))
    }

    override fun sendDismiss(
        device: NsdDevice,
        sdp: String
    ) {
        device.sendAction(SDPDismiss(device, sdp))
    }

    override fun sendOffer(
        device: NsdDevice,
        sdp: String
    ) {
        device.sendAction(SDPOffer(device, sdp))
    }

    override fun sendAnswer(
        device: NsdDevice,
        sdp: String
    ) {
        device.sendAction(SDPAnswer(device, sdp))
    }

    override fun sendCallStart(
        caller: NsdDevice,
        callee: NsdDevice
    ) {
        callee.sendAction(SDPStartCall(caller, callee))
    }

    override fun sendCallStarted(
        caller: NsdDevice,
        callee: NsdDevice
    ) {
        caller.sendAction(SDPStartCallStarted(caller, callee))
    }

    override fun registerCallManager(callManager: ICallManager) {
        callManagers.add(callManager)
    }

    override fun unregisterCallManager(callManager: ICallManager) {
        callManagers.remove(callManager)
    }

    override fun getCallManagers(): List<ICallManager> =
        callManagers

    companion object {
        val TAG = NsdDevice::class.simpleName

        fun NsdRouting.actionSDPStartCall() = createAction<SDPStartCall>()
        fun NsdRouting.actionSDPStartCallStarted() = createAction<SDPStartCallStarted>()
        fun NsdRouting.actionSDPIceCandidate() = createAction<SDPIceCandidate>()
        fun NsdRouting.actionSDPAccept() = createAction<SDPAccept>()
        fun NsdRouting.actionSDPAnswer() = createAction<SDPAnswer>()
        fun NsdRouting.actionSDPDismiss() = createAction<SDPDismiss>()
        fun NsdRouting.actionSDPOffer() = createAction<SDPOffer>()

        inline fun <reified T : NsdAction> NsdRouting.createAction(
            crossinline onError: (Throwable, RoutingCall) -> Boolean = { _, _ -> false }
        ): Route {
            val routePath = T::class.simpleName
            return post("/$routePath") {
                runCatching {
                    call.receive<T>().also { action ->
                        onAction(action)
                    }
                }.onFailure { e ->
                    if (!onError(e, call)) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                    }
                }.onSuccess {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }

        inline fun <reified T : NsdAction> NsdDevice.sendAction(
            action: T
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    val route = action::class.simpleName
                    val address = this@sendAction.address
                    val port = this@sendAction.port
                    val url = "http://$address:$port/$route"
                    val jsonString = action.asJson()
                    val body = jsonString.toRequestBody("application/json".toMediaType())
                    OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().apply {
                            level = Level.BODY
                        })
                        .build()
                        .newCall(
                            Request.Builder()
                                .url(url)
                                .post(body)
                                .build()
                        )
                        .execute()
                        .use { response ->
                            if (response.isSuccessful) {
                                Log.d(TAG, "Success to send action: ${response.code} to $address")
                                Log.d(TAG, "Url: $url")
                                Log.d(TAG, "Action $action send to $address")
                                Log.d(TAG, "Data: $jsonString")
                            } else {
                                Log.e(TAG, "Failed to send action: ${response.code} to $address")
                                Log.e(TAG, "Url: $url")
                                Log.e(TAG, "Action: $action")
                                Log.d(TAG, "Data: $jsonString")
                                Log.e(TAG, response.message)
                            }
                        }
                }.onFailure { e ->
                    e.printStackTrace()
                }
            }
        }

        fun Context.sendActionToAll(
            types: List<NsdType> = NsdType.entries,
            action: NsdAction,
        ) = nsdDevices(types) { devices ->
            devices.forEach { device ->
                if (device.address != currentWifiIP) {
                    device.sendAction(action)
                }
            }
        }
    }
}
