package org.mjdev.phone.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.mjdev.phone.nsd.device.NsdDevice
import org.webrtc.IceCandidate

object NsdActions {

    @Serializable
    @SerialName("NsdActionCall")
    open class SDPStartCall(
        open val caller: NsdDevice?,
        open val callee: NsdDevice?
    ) : NsdAction()

    @Serializable
    @SerialName("SDPStartCallStarted")
    open class SDPStartCallStarted(
        open val caller: NsdDevice?,
        open val callee: NsdDevice?
    ) : NsdAction()

    @Serializable
    @SerialName("SDPIceCandidate")
    data class SDPIceCandidate(
        val device: NsdDevice?,
        val sdpMid: String,
        val sdpMLineIndex: Int,
        val sdp: String,
    ) : NsdAction() {
        constructor(
            device: NsdDevice?,
            iceCandidate: IceCandidate
        ) : this(
            device = device,
            sdpMid = iceCandidate.sdpMid,
            sdpMLineIndex = iceCandidate.sdpMLineIndex,
            sdp = iceCandidate.sdp
        )
    }

    @Serializable
    @SerialName("SDPAnswer")
    data class SDPAnswer(
        val device: NsdDevice?,
        val sdp: String,
    ) : NsdAction()

    @Serializable
    @SerialName("SDPOffer")
    data class SDPOffer(
        val device: NsdDevice?,
        val sdp: String,
    ) : NsdAction()

    @Serializable
    @SerialName("SDPDismiss")
    data class SDPDismiss(
        val device: NsdDevice?,
        val sdp: String,
    ) : NsdAction()

    @Serializable
    @SerialName("SDPAccept")
    data class SDPAccept(
        val device: NsdDevice?,
        val sdp: String,
    ) : NsdAction()

}