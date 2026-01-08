package org.mjdev.phone.rpc

import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.util.Log
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.device.nsdDeviceListFlow
import org.mjdev.phone.nsd.rpc.INsdServerRPC
import kotlin.reflect.KClass
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP

@Suppress("unused", "RedundantSuspendModifier")
@OptIn(
    ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class,
    InternalSerializationApi::class
)
open class NsdServerRpc(
    context: Context,
    var onStarted: (String, Int) -> Unit = { a, p -> },
    var onStopped: () -> Unit = {},
    val onAction: (NsdAction) -> Unit = {},
    val additionalRoutes: NsdRouting.() -> Unit = {},
) : INsdServerRPC(context) {
    override val address: String
        get() = context.currentWifiIP
    override var port: Int = 8888
    internal  set
    override var isRunning: Boolean = false
        internal set

    fun addJsonType(type: KClass<out NsdAction>) {
        nsdActionsRegister.register(type)
    }

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
            install(ContentNegotiation) {
                json(nsdActionsRegister.json)
            }
            routing {
                NsdRoutingContext(this, this@NsdServerRpc, onAction).apply {
                    actionCall()
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

    companion object {
        val TAG = NsdDevice::class.simpleName

        val nsdActionsRegister = NsdActionRegistry()

        fun NsdRouting.actionCall() = createAction<NsdActions.NsdActionCall>()

        inline fun <reified T : NsdAction> NsdRouting.createAction(
            crossinline onError: (Throwable, RoutingCall) -> Boolean = { _, _ -> false }
        ): Route {
            val route = T::class.simpleName
            this.nsdServerRpc.addJsonType(T::class)
            return post("/$route") {
                runCatching {
                    call.receive<T>().also { action ->
                        onAction(action)
                    }
                }.onFailure { e ->
                    if (!onError(e, call)) {
                        call.respond(HttpStatusCode.InternalServerError, e)
                    }
                }.onSuccess {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }

        suspend inline fun <reified T : NsdAction> NsdDevice.sendAction(
            action: T
        ) = runCatching {
            val route = action::class.simpleName
            val address = this@sendAction.address ?: return@runCatching
            val url = "http://$address:$port/$route"
            val jsonString = nsdActionsRegister.json.encodeToString(action)
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
                    if (response.isSuccessful.not()) {
                        Log.e(TAG, "Failed to send action: ${response.code} to $address")
                        Log.e(TAG, "Url: $url")
                        Log.e(TAG, "Action: $action")
                        Log.e(TAG, response.message)
                    } else {
                        Log.d(TAG, "Action $action send to $address")
                        Log.d(TAG, "Data: $jsonString")
                    }
                }
        }.onFailure { e ->
            e.printStackTrace()
        }

        suspend fun Context.sendActionToAll(
            types: List<NsdTypes> = NsdTypes.entries,
            onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
            filter: (NsdServiceInfo) -> Boolean = { true },
            action: NsdAction,
        ) = nsdDeviceListFlow(this, types, onError, filter).collectLatest { devices ->
            devices.forEach { device ->
                device.sendAction(action)
            }
        }

        @Suppress("UnusedReceiverParameter")
        suspend fun Context.makeCall(
            caller: NsdDevice?,
            callee: NsdDevice,
        ) {
            callee.sendAction(NsdActions.NsdActionCall(caller, callee))
        }

        class StartupPluginConfig(
            var onStart: (address: String, port: Int) -> Unit = { _, _ -> }
        )

        @Suppress("DEPRECATION")
        val StartupPlugin = createApplicationPlugin(
            name = "StartupPlugin",
            createConfiguration = ::StartupPluginConfig
        ) {
            application.environment.monitor.subscribe(ApplicationStarted) {
                CoroutineScope(Dispatchers.IO).launch {
                    val connectors = application.engine.resolvedConnectors()
                    val firstConnector = connectors.first()
                    val address = firstConnector.host
                    val port = firstConnector.port
                    withContext(Dispatchers.Main) {
                        pluginConfig.onStart(address, port)
                    }
                }
            }
        }

        class StopPluginConfig(
            var onStop: () -> Unit = {}
        )

        @Suppress("DEPRECATION")
        val StopingPlugin = createApplicationPlugin(
            name = "StopingPlugin",
            createConfiguration = ::StopPluginConfig
        ) {
            application.environment.monitor.subscribe(ApplicationStopping) {
                CoroutineScope(Dispatchers.Main).launch {
                    pluginConfig.onStop()
                }
            }
        }
    }
}
