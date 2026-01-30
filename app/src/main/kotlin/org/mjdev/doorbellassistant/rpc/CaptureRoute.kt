/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.rpc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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
import org.mjdev.doorbellassistant.rpc.DoorBellActions.DoorBellActionMotionDetected
import org.mjdev.doorbellassistant.rpc.DoorBellActions.DoorBellActionMotionUnDetected
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdType
import org.mjdev.phone.rpc.server.NsdServerRpc.Companion.sendActionToAll
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@Suppress("RedundantSuspendModifier")
object CaptureRoute {

    const val ROUTE_CAPTURE = "/capture"

    fun Route.captureRoute(
        routeName: String = ROUTE_CAPTURE
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
        if (this === NsdDevice.EMPTY) return null
        val address = this@getFrame.address
        val port = this@getFrame.port
        if (address.isNullOrEmpty()) {
            return null
        } else {
            val url = "http://$address:$port/$routeName"
            OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = Level.BASIC  // Reduce logging verbosity
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
                            "Failed to get frame from ($address:$port): ${response.code}"
                        )
                        null
                    } else {
                        response.body.byteStream().let { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                }
        }
    }.onFailure { e ->
        Log.e(
            NsdDevice.TAG,
            "Error getting frame from ${this@getFrame.address}:${this@getFrame.port}",
            e
        )
    }.getOrNull()

    suspend fun Context.sendMotionDetected(
        sender: NsdDevice,
        types: List<NsdType> = listOf(NsdType.DOOR_BELL_CLIENT),
    ) = sendActionToAll(
        types,
        DoorBellActionMotionDetected(sender)
    )

    suspend fun Context.sendMotionUnDetected(
        sender: NsdDevice,
        types: List<NsdType> = listOf(NsdType.DOOR_BELL_CLIENT),
    ) = sendActionToAll(
        types,
        DoorBellActionMotionUnDetected(sender)
    )
}
