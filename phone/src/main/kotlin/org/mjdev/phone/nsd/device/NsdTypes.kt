package org.mjdev.phone.nsd.device

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.ConnectedTv
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Suppress("unused")
@Serializable
enum class NsdTypes(
    @Transient
    val imageVector: ImageVector = Icons.Filled.DeviceUnknown,
    private val uid: String,
    val label: String,
    val isAutoAnswerCall: Boolean = false,
    val micMutedAtStart: Boolean = false,
    val speakerOnAtStart: Boolean = false,
    val userPhoto: ByteArray? = null,
) {
    UNSPECIFIED(
        imageVector = Icons.Filled.DeviceUnknown,
        uid = "unspecified",
        label = "Unknown device",
        isAutoAnswerCall = false,
        micMutedAtStart = false,
        speakerOnAtStart = true
    ),
    DOOR_BELL_ASSISTANT(
        imageVector = Icons.Filled.CameraRear,
        uid = "db-assistant",
        label = "Doorbell Assistant",
        isAutoAnswerCall = true, // todo : when app is set as assistant change nsd state
        micMutedAtStart = false,
        speakerOnAtStart = true
    ),
    DOOR_BELL_CLIENT(
        imageVector = Icons.Filled.ConnectedTv,
        uid = "db-client",
        label = "Doorbell Assistant Client",
        isAutoAnswerCall = false,
        micMutedAtStart = false,
        speakerOnAtStart = false
    ),
    SAFE_DIALER(
        imageVector = Icons.Filled.PhoneAndroid,
        uid = "phone",
        label = "Phone",
        isAutoAnswerCall = false,
        micMutedAtStart = false,
        speakerOnAtStart = true
    );

    companion object {
        val NsdTypes.serviceName
            get() = "_${uid}._tcp"

        val entriesMap = entries.associateBy { entry -> "." + entry.serviceName }

        operator fun invoke(
            uid: String?
        ): NsdTypes {
            val deviceId = uid?.let { id ->
                if (id.startsWith(".")) id else ".$id"
            } ?: ""
            val typeFromUID = entriesMap[deviceId]
            return typeFromUID ?: UNSPECIFIED
        }
    }
}
