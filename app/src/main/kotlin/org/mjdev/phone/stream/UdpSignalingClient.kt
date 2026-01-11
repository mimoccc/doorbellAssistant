package org.mjdev.phone.stream

import android.util.Log
import org.mjdev.phone.stream.UdpSignalingServer.Companion.TAG
import org.webrtc.IceCandidate

class UdpSignalingClient(
    private var remoteIp: String,
    private val remotePort: Int = 8889,
    private val localPort: Int = 8889,
    private val onFailure: (Throwable) -> Unit = { e ->
        Log.e(TAG, "Failed to start UDP signaling", e)
    },
    private val onReady: () -> Unit = {},
    private var onOfferReceived: (String) -> Unit = {},
    private var onAnswerReceived: (String) -> Unit = {},
    private var onIceCandidateReceived: (IceCandidate) -> Unit = {},
    private val onDismissReceived: (CallEndReason) -> Unit = {},
    private val onAcceptReceived: () -> Unit = {},
) {
    private val signalingServer = UdpSignalingServer(
        port = localPort,
        onFailure = onFailure,
        onReady =  onReady,
        onOfferReceived = onOfferReceived,
        onAnswerReceived = onAnswerReceived,
        onIceCandidateReceived = onIceCandidateReceived,
        onDismissReceived = onDismissReceived,
        onAcceptReceived = onAcceptReceived
    )

    fun sendOffer(
        sdp: String,
        address: String = remoteIp,
        port: Int = remotePort,
    ) {
        signalingServer.sendMessage(SDPMessage.SDPOffer(sdp), address, port)
    }

    fun sendAnswer(
        sdp: String,
        address: String = remoteIp,
        port: Int = remotePort,
    ) {
        signalingServer.sendMessage(SDPMessage.SDPAnswer(sdp), address, port)
    }

    fun sendIceCandidate(
        candidate: IceCandidate,
        address: String = remoteIp,
        port: Int = remotePort,
    ) {
        signalingServer.sendMessage(SDPMessage.SDPIceCandidate(candidate), address, port)
    }

    fun sendDismiss(
        sdp: String,
        address: String = remoteIp,
        port: Int = remotePort,
    ) {
        signalingServer.sendMessage(SDPMessage.SDPDismiss(sdp), address, port)
    }

    fun sendAccept(
        sdp: String,
        address: String = remoteIp,
        port: Int = remotePort,
    ) {
        signalingServer.sendMessage(SDPMessage.SDPAccept(sdp), address, port)
    }

    fun stop() {
        signalingServer.stop()
    }

    fun start() {
        signalingServer.start()
    }
}
