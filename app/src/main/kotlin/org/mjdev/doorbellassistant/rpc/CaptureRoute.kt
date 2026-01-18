package org.mjdev.doorbellassistant.rpc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.glance.color.DynamicThemeColorProviders.onError
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.mjdev.doorbellassistant.helpers.MotionDetector
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.rpc.server.NsdServerRpc.Companion.sendActionToAll
import org.mjdev.doorbellassistant.rpc.DoorBellActions.DoorBellActionMotionUnDetected
import org.mjdev.doorbellassistant.rpc.DoorBellActions.DoorBellActionMotionDetected
import java.io.ByteArrayOutputStream

@Suppress("RedundantSuspendModifier")
object CaptureRoute {

    const val ROUTE_CAPTURE = "/capture"

    fun Route.captureRoute(
        routeName : String = ROUTE_CAPTURE
    ) = get(routeName) {
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

    suspend fun NsdDevice.getFrame(
        routeName: String = ROUTE_CAPTURE
    ): Bitmap? = runCatching {
        val address = this@getFrame.address
        val port = this@getFrame.port
        if (address.isEmpty()) {
            return null
        } else {
            val url = "http://$address:$port/$routeName"
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
    }.onFailure { e->
        e.printStackTrace()
    }.getOrNull()

    suspend fun Context.sendMotionDetected(
        sender: NsdDevice?,
        types: List<NsdTypes> = listOf(NsdTypes.DOOR_BELL_CLIENT),
    ) = sendActionToAll(
        types,
        DoorBellActionMotionDetected(sender)
    )

    suspend fun Context.sendMotionUnDetected(
        sender: NsdDevice?,
        types: List<NsdTypes> = listOf(NsdTypes.DOOR_BELL_CLIENT),
    ) = sendActionToAll(
        types,
        DoorBellActionMotionUnDetected(sender)
    )

}
