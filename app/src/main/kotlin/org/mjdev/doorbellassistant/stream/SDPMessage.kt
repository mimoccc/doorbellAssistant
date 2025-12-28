package org.mjdev.doorbellassistant.stream

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.webrtc.IceCandidate

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
@Serializable
sealed class SDPMessage {

    @Serializable
    @SerialName("SDPIceCandidate")
    data class SDPIceCandidate(
        val sdpMid: String,
        val sdpMLineIndex: Int,
        val candidate: String,
    ) : SDPMessage() {
        val type: String = "candidate"

        constructor(
            iceCandidate: IceCandidate
        ) : this(
            sdpMid = iceCandidate.sdpMid,
            sdpMLineIndex = iceCandidate.sdpMLineIndex,
            candidate = iceCandidate.sdp
        )
    }

    @Serializable
    @SerialName("SDPAnswer")
    data class SDPAnswer(
        val sdp: String,
    ) : SDPMessage() {
        val type: String = "answer"
    }

    @Serializable
    @SerialName("SDPOffer")
    data class SDPOffer(
        val sdp: String,
    ) : SDPMessage() {
        val type: String = "offer"
    }

    @Serializable
    @SerialName("SDPDismiss")
    data class SDPDismiss(
        val sdp: String,
    ) : SDPMessage() {
        val type: String = "dismiss"
    }

}
