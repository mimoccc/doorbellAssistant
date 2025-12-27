package org.mjdev.doorbellassistant.helpers.nsd.device

import android.R.attr.host
import android.R.attr.port
import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.compose.ui.graphics.vector.ImageVector
import io.netty.channel.unix.NativeInetAddress.address
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.net.InetAddress

@Suppress("TRANSIENT_IS_REDUNDANT", "unused")
@Serializable
data class NsdDevice(
    @Transient
    var nsdServiceInfo: NsdServiceInfo? = null,
    val serviceName: String? = nsdServiceInfo?.serviceName ?: "",
    val serviceTypeUid: String? = nsdServiceInfo?.serviceType ?: "",
    @Suppress("DEPRECATION")
    val address: String? = nsdServiceInfo?.host?.hostAddress ?: "",
    val port: Int = nsdServiceInfo?.port ?: 8888,
) {
    @Transient
    val hostName: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
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

        fun fromData(
            address: String,
            serviceType: NsdTypes = NsdTypes.DOOR_BELL_ASSISTANT,
            serviceName: String? = "",
            port: Int = 8888,
        ) = NsdDevice(NsdServiceInfo().apply {
            this.port = port
            this.serviceType = serviceType.uid
            this.serviceName = serviceName
            @Suppress("DEPRECATION")
            host = InetAddress.getByName(address)
        })
    }
}
