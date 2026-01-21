/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.rpc.action

import org.mjdev.phone.helpers.json.Serializable
import org.mjdev.phone.nsd.device.NsdDevice
import org.webrtc.IceCandidate

object NsdActions {
    @Serializable
    data class SDPGetState(
        val sender: NsdDevice?,
    ) : NsdAction()

    @Serializable
    data class SDPState(
        val receiver: NsdDevice?,
    ) : NsdAction()

    @Serializable
    data class SDPStartCall(
        val caller: NsdDevice?,
        val callee: NsdDevice?
    ) : NsdAction()

    @Serializable
    data class SDPStartCallStarted(
        val caller: NsdDevice?,
        val callee: NsdDevice?
    ) : NsdAction()

    @Serializable
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
    data class SDPAnswer(
        val device: NsdDevice?,
        val sdp: String,
    ) : NsdAction()

    @Serializable
    data class SDPOffer(
        val device: NsdDevice?,
        val sdp: String,
    ) : NsdAction()

    @Serializable
    data class SDPDismiss(
        val device: NsdDevice?,
        val sdp: String,
    ) : NsdAction()

    @Serializable
    data class SDPAccept(
        val device: NsdDevice?,
        val sdp: String,
    ) : NsdAction()
}
