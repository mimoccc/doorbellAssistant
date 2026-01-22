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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.ConnectedTv
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector
import org.mjdev.phone.helpers.json.DontSerialize
import org.mjdev.phone.helpers.json.Serializable

@Serializable
@Suppress("ClassName", "unused", "ANNOTATION_WILL_BE_APPLIED_ALSO_TO_PROPERTY_OR_FIELD")
open class NsdType(
    @DontSerialize
    val imageVector: ImageVector = Icons.Filled.DeviceUnknown,

    val uid: String,
    val label: String,
    val isAutoAnswerCall: Boolean = false,
    val micMutedAtStart: Boolean = false,
    val speakerOnAtStart: Boolean = false,
    val userPhoto: ByteArray? = null,
) {
    @Serializable
    object UNSPECIFIED : NsdType(
        imageVector = Icons.Filled.DeviceUnknown,
        uid = "unspecified",
        label = "Unknown device",
        isAutoAnswerCall = false,
        micMutedAtStart = false,
        speakerOnAtStart = true
    )

    @Serializable
    object DOOR_BELL_ASSISTANT : NsdType(
        imageVector = Icons.Filled.CameraRear,
        uid = "db-assistant",
        label = "Doorbell Assistant",
        isAutoAnswerCall = true,
        micMutedAtStart = false,
        speakerOnAtStart = true
    )

    @Serializable
    object DOOR_BELL_CLIENT : NsdType(
        imageVector = Icons.Filled.ConnectedTv,
        uid = "db-client",
        label = "Doorbell Assistant Client",
        isAutoAnswerCall = false,
        micMutedAtStart = false,
        speakerOnAtStart = false
    )

    @Serializable
    object SAFE_DIALER : NsdType(
        imageVector = Icons.Filled.PhoneAndroid,
        uid = "phone",
        label = "Phone",
        isAutoAnswerCall = false,
        micMutedAtStart = false,
        speakerOnAtStart = true
    )

    companion object {
        @DontSerialize
        val entries by lazy {
            mutableListOf(
                UNSPECIFIED,
                DOOR_BELL_ASSISTANT,
                DOOR_BELL_CLIENT,
                SAFE_DIALER
            )
        }

        fun registerType(type: NsdType) {
            entries.add(type)
        }

        fun unRegisterType(type: NsdType) {
            entries.remove(type)
        }

        @DontSerialize
        val NsdType.serviceTypeName
            get() = "_${uid}._tcp"

        @DontSerialize
        private val entriesMap
            get() = entries.associateBy { entry -> ".${entry.serviceTypeName}" }

        operator fun invoke(
            uid: String?
        ): NsdType {
            val deviceId = uid?.let { id ->
                if (id.startsWith(".")) id else ".$id"
            } ?: ""
            val typeFromUID = entriesMap[deviceId]
            return typeFromUID ?: UNSPECIFIED
        }
    }
}
