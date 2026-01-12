package org.mjdev.phone.nsd.device

import android.net.nsd.NsdServiceInfo
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.mjdev.phone.extensions.CustomExtensions.toInetAddress
import org.mjdev.phone.nsd.device.NsdTypes.Companion.serviceName

@Suppress("TRANSIENT_IS_REDUNDANT", "unused", "DEPRECATION")
@Serializable
open class NsdDevice {
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

    @Transient
    val imageVector: ImageVector
        get() = serviceType.imageVector

    @Transient
    val label: String
        get() = serviceType.label

    @Transient
    val isAutoAnswerCall: Boolean
        get() = serviceType.isAutoAnswerCall

    override fun toString(): String {
        return "[$serviceType]($address:$port)"
    }

    companion object {
        @Transient
        val TAG = NsdDevice::class.simpleName

        val EMPTY get() = fromData("192.168.1.1","Test")

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
