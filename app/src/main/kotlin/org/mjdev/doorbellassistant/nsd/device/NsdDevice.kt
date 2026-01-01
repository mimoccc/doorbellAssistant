package org.mjdev.doorbellassistant.nsd.device

import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.mjdev.doorbellassistant.extensions.ComposeExt.toInetAddress
import java.net.InetAddress
import java.net.UnknownHostException

@Suppress("TRANSIENT_IS_REDUNDANT", "unused", "DEPRECATION")
@Serializable
open class NsdDevice(
    @Transient
    var nsdServiceInfo: NsdServiceInfo? = null,
    val serviceName: String? = nsdServiceInfo?.serviceName ?: "",
    val serviceTypeUid: String? = nsdServiceInfo?.serviceType ?: "",
    val address: String? = nsdServiceInfo?.host?.hostAddress ?: "",
    val port: Int = nsdServiceInfo?.port ?: 8888,
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
        get() = NsdTypes(serviceTypeUid)

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
            address: String,
            serviceName: String,
            serviceType: NsdTypes = NsdTypes.DOOR_BELL_ASSISTANT,
            port: Int = 8888,
        ) = NsdDevice(NsdServiceInfo().apply {
            this.port = port
            this.serviceType = serviceType.uid
            this.serviceName = serviceName
            host = address.toInetAddress()
        })
    }
}
