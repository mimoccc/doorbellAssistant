/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.nsd.device

import android.annotation.SuppressLint
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.compose.ui.graphics.vector.ImageVector
import org.mjdev.phone.data.DeviceDetails
import org.mjdev.phone.data.UserDetails
import org.mjdev.phone.extensions.StringExt.toInetAddress
import org.mjdev.phone.helpers.json.Serializable
import org.mjdev.phone.nsd.device.NsdType.Companion.serviceTypeName

@Suppress("UNCHECKED_CAST")
@Serializable
class NsdDevice {
    var serviceId: String?
    var address: String?
    var port: Int
    var serviceTypeName: String?
    var attributes: MutableMap<String, String>

    // todo, user picture from device
    var photo: ImageVector?

    constructor() {
        serviceId = ""
        serviceTypeName = NsdType.UNSPECIFIED.serviceTypeName
        address = "0.0.0.0"
        port = 0
        attributes = mutableMapOf()
        photo = null
    }

    constructor(nsdServiceInfo: NsdServiceInfo) {
        serviceId = nsdServiceInfo.serviceName
        serviceTypeName = nsdServiceInfo.serviceType
        address = nsdServiceInfo.hostAddress
        port = nsdServiceInfo.port
        attributes = nsdServiceInfo.attributes.map { (k, v) ->
            k to String(v)
        }.toMap().toMutableMap()
        photo = null
    }

    @Suppress("DEPRECATION")
    fun toNsdServiceInfo(): NsdServiceInfo = NsdServiceInfo().apply {
        serviceName = serviceId
        serviceType = this@NsdDevice.serviceType.serviceTypeName
        host = address?.toInetAddress()
        port = this@NsdDevice.port
        this@NsdDevice.attributes.forEach { (k, v) ->
            if (v.length > 255) {
                Log.w(TAG, "The value for TXT nsd record is max 255 chars.")
                Log.w(TAG, "Property $k exceeds size, will be truncated.")
            }
            setAttribute(k, v.take(255))
        }
    }

    override fun toString(): String {
        return "[$serviceType]($address:$port)"
    }

    companion object {
        val TAG = NsdDevice::class.simpleName

        var NsdDevice.serviceType
            get() = NsdType(this.serviceTypeName)
            set(value) {
                this.serviceTypeName = value.serviceTypeName
            }

        val NsdDevice.imageVector: ImageVector
            get() = serviceType.imageVector

        val NsdDevice.label: String?
            get() = serviceType.label

        val NsdDevice.details: String?
            get() = if (device.isEmpty()) null
            else "${device.manufacturer.uppercase()} - ${device.model.uppercase()}"

        val NsdDevice.isAutoAnswerCall: Boolean
            get() = serviceType.isAutoAnswerCall

        var NsdDevice.user: UserDetails
            get() = mapAttrs()
            set(value) = setAttrs(value)

        var NsdDevice.device: DeviceDetails
            get() = mapAttrs()
            set(value) = setAttrs(value)

        val EMPTY by lazy {
            NsdDevice().apply {
                address = "0.0.0.0"
                serviceId = "" // ANDROID_ID
                serviceTypeName = NsdType.UNSPECIFIED.serviceTypeName
                device = DeviceDetails.THIS
                user = UserDetails()
            }
        }

        val NsdServiceInfo.hostAddress: String
            get() = runCatching {
                @Suppress("DEPRECATION")
                @SuppressLint("NewApi")
                hostAddresses.firstOrNull()?.hostAddress ?: host.hostAddress
            }.onFailure { e ->
                e.printStackTrace()
            }.getOrNull() ?: "0.0.0.0"

        private inline fun <reified T : INsdDetail> NsdDevice.mapAttrs(): T {
            return attributes.values.map { textValue ->
                textValue.split("=")
            }.mapNotNull { nv ->
                if (nv.size > 1) Pair(nv[0], nv[1]) else null
            }.toMap().let { map ->
                DeviceDetails(map.toMutableMap()) as T
            }
        }

        private inline fun <reified T : INsdDetail> NsdDevice.setAttrs(attrs: T) {
            attrs.mapAs { e ->
                Pair(e.key, e.value)
            }.forEachIndexed { idx, e ->
                attributes[idx.toString()] = "${e.first}=${e.second}".take(255)
            }
        }
    }
}
