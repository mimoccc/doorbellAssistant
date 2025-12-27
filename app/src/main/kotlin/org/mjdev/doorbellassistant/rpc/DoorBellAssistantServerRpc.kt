package org.mjdev.doorbellassistant.rpc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.nsd.NsdServiceInfo
import android.util.Log
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.mjdev.doorbellassistant.helpers.MotionDetector
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdTypes
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdTypes.DOOR_BELL_CLIENT
import org.mjdev.doorbellassistant.helpers.nsd.device.nsdDeviceListFlow
import org.mjdev.doorbellassistant.helpers.nsd.rpc.INsdServerRPC
import java.io.ByteArrayOutputStream
import org.mjdev.doorbellassistant.rpc.DoorBellAction.DoorBellActionMotionDetected

@Suppress("unused", "RedundantSuspendModifier")
@OptIn(ExperimentalCoroutinesApi::class)
class DoorBellAssistantServerRpc(
    context: Context,
    port: Int = 8888,
    val onAction: (DoorBellAction) -> Unit = {}
) : INsdServerRPC(context, port) {
    @OptIn(ExperimentalSerializationApi::class)
    private val server by lazy {
        embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json(json)
            }
            routing {
                post("/action") {
                    runCatching {
                        call.receive<DoorBellAction>().also { action ->
                            onAction(action)
                        }
                    }.onFailure { e ->
                        call.respond(HttpStatusCode.InternalServerError, e)
                    }.onSuccess {
                        call.respond(HttpStatusCode.OK)
                    }
                }
                get("/capture") {
                    runCatching {
                        val image = MotionDetector.latestBitmap.value
                        if (image != null) {
                            val stream = ByteArrayOutputStream()
                            image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            stream.toByteArray()
                        } else null
                    }.onFailure { e ->
                        call.respond(HttpStatusCode.InternalServerError, e)
                    }.onSuccess { image ->
                        if (image != null) {
                            call.respondBytes(image, ContentType.Image.JPEG)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "No image captured")
                        }
                    }
                }
            }
        }
    }

    override suspend fun start() {
        server.start(wait = false)
    }

    override suspend fun stop() {
        server.stop(1000, 2000)
    }

    companion object {
        val TAG = NsdDevice::class.simpleName

        val json = Json {
            prettyPrint = true
            isLenient = true
            encodeDefaults = true
            ignoreUnknownKeys = true
            classDiscriminator = "type"
//            explicitNulls = true
//            coerceInputValues = true
//            classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
//            decodeEnumsCaseInsensitive = true
//            allowStructuredMapKeys = true
        }

        suspend inline fun <reified T : DoorBellAction> NsdDevice.sendAction(
            action: T
        ) {
            val address = address ?: return
            val url = "http://$address:$port/action"
            val jsonString = json.encodeToString(action)
            val body = jsonString.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = Level.BODY
                })
                .build()
                .newCall(request)
                .execute().use { response ->
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
        }

        suspend fun NsdDevice.getFrame(): Bitmap? {
            val address = address ?: return null
            val url = "http://$address:$port/capture"
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = Level.BODY
                })
                .build()
                .newCall(
                    Request.Builder()
                        .url(url)
                        .build()
                ).execute().use { response ->
                    return if (!response.isSuccessful) {
                        Log.e(
                            NsdDevice.TAG,
                            "Failed to get frame from ($address): ${response.code}"
                        )
                        null
                    } else {
                        response.body.byteStream().let { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                }
        }

        suspend fun Context.sendActionToAll(
            types: List<NsdTypes> = listOf(DOOR_BELL_CLIENT),
            onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
            filter: (NsdServiceInfo) -> Boolean = { true },
            action: DoorBellAction,
        ) = nsdDeviceListFlow(this, types, onError, filter).collectLatest { devices ->
            devices.forEach { device ->
                device.sendAction(action)
            }
        }

        suspend fun Context.sendMotionDetected(
            sender: NsdDevice,
            types: List<NsdTypes> = listOf(DOOR_BELL_CLIENT),
            onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
            filter: (NsdServiceInfo) -> Boolean = { true },
        ) = sendActionToAll(
            types,
            onError,
            filter,
            DoorBellActionMotionDetected(sender)
        )

        suspend fun Context.sendMotionUnDetected(
            sender: NsdDevice,
            types: List<NsdTypes> = listOf(DOOR_BELL_CLIENT),
            onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
            filter: (NsdServiceInfo) -> Boolean = { true },
        ) = sendActionToAll(
            types,
            onError,
            filter,
            DoorBellAction.DoorBellActionMotionUnDetected(sender)
        )
    }
}
