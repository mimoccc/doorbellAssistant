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

import android.net.nsd.NsdServiceInfo
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.mjdev.phone.extensions.StringExt.toInetAddress
import org.mjdev.phone.nsd.device.NsdTypes.Companion.serviceName

@Serializable
open class NsdDevice {
    @Suppress("DEPRECATION")
    constructor(nsdServiceInfo: NsdServiceInfo) {
        address = nsdServiceInfo.host?.hostAddress ?: ""
        port = nsdServiceInfo.port
        serviceName = nsdServiceInfo.serviceName ?: ""
        hostName = nsdServiceInfo.host?.hostName ?: address
        serviceType = NsdTypes(nsdServiceInfo.serviceType)
        serviceTypeName = serviceType.serviceName
    }

    var hostName: String
    var serviceName: String
    var serviceTypeName: String
    var address: String
    var port: Int
    var serviceType: NsdTypes

    val imageVector: ImageVector
        get() = serviceType.imageVector

    val label: String
        get() = serviceType.label

    val isAutoAnswerCall: Boolean
        get() = serviceType.isAutoAnswerCall

    override fun toString(): String {
        return "[$serviceType]($address:$port)"
    }

    companion object {
        @Transient
        val TAG = NsdDevice::class.simpleName

        val EMPTY get() = fromData("192.168.1.1","Test")

        @Suppress("DEPRECATION")
        fun fromData(
            address: String = "0.0.0.0",
            serviceName: String = "",
            serviceType: NsdTypes = NsdTypes.UNSPECIFIED,
            port: Int = 8888,
        ) = NsdDevice(NsdServiceInfo().apply {
            this.port = port
            this.serviceType = serviceType.serviceName
            this.serviceName = serviceName
            this.host = address.toInetAddress()
        })
    }
}
