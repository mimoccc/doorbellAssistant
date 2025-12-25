package org.mjdev.doorbellassistant.helpers.nsd

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.ConnectedTv
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.ui.graphics.vector.ImageVector

enum class NsdTypes(
    val imageVector: ImageVector,
    val uid: String,
    val label : String
) {
    UNSPECIFIED(
        imageVector = Icons.Filled.DeviceUnknown,
        uid = "unspecified",
        label = "Unknown device"
    ),
    DOOR_BELL_ASSISTANT(
        imageVector = Icons.Filled.CameraRear,
        uid = "db-assistant",
        label = "Doorbell Assistant"
    ),
    DOOR_BELL_CLIENT(
        imageVector = Icons.Filled.ConnectedTv,
        uid = "db-client",
        label = "Doorbell Assistant Client"
    );

    companion object {
        val NsdTypes.serviceName
            get() = "_${uid}._tcp"

        operator fun invoke(
            uid: String
        ) = entries.firstOrNull { entry ->
            uid.contains(entry.serviceName)
        } ?: UNSPECIFIED
    }
}
