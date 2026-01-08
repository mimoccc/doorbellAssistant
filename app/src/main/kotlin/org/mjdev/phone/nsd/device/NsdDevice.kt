package org.mjdev.phone.nsd.device

import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.mjdev.phone.extensions.CustomExtensions.toInetAddress
import org.mjdev.phone.service.CallNsdService

@Suppress("TRANSIENT_IS_REDUNDANT", "unused", "DEPRECATION")
@Serializable
open class NsdDevice(
    @Transient
    var nsdServiceInfo: NsdServiceInfo? = null,
    val serviceName: String? = nsdServiceInfo?.serviceName ?: "",
    val serviceTypeUid: String? = nsdServiceInfo?.serviceType ?: "",
    val address: String? = nsdServiceInfo?.host?.hostAddress ?: "",
    val port: Int? = nsdServiceInfo?.port,
) {
    @Transient
    val hostName: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            nsdServiceInfo?.host?.hostName
        } else {
            null
        }

    @Transient
    val serviceType: NsdTypes
        get() =  NsdTypes.Companion(serviceTypeUid)

    @Transient
    val imageVector: ImageVector
        get() = serviceType.imageVector

    @Transient
    val uid: String
        get() = serviceType.uid

    @Transient
    val label: String
        get() = serviceType.label

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
            this.serviceType = serviceType.uid
            this.serviceName = serviceName
            this.host = address.toInetAddress()
        })
    }
}
