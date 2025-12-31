package org.mjdev.doorbellassistant.stream

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.mjdev.doorbellassistant.extensions.ComposeExt.asJson
import org.mjdev.doorbellassistant.stream.SDPMessage.SDPAnswer
import org.mjdev.doorbellassistant.stream.SDPMessage.SDPIceCandidate
import org.mjdev.doorbellassistant.stream.SDPMessage.SDPOffer
import org.mjdev.doorbellassistant.stream.SDPMessage.SDPDismiss
import org.webrtc.IceCandidate
import java.net.DatagramPacket
import java.net.InetAddress

class UdpSignalingClient(
    private var remoteIp: String,
    private val signalingPort: Int = 8889,
    private val socketTimeout: Int = 30000,
    private var onOfferReceived: (String) -> Unit = {},
    private var onAnswerReceived: (String) -> Unit = {},
    private var onIceCandidateReceived: (IceCandidate) -> Unit = {},
    private val onDismissReceived: (CallEndReason) -> Unit = {},
    private val onAcceptReceived: () -> Unit = {},
) {
    companion object {
        private val TAG = UdpSignalingClient::class.simpleName
    }

    @Volatile
    private var listenerThread: UDPThread? = null

    fun connect(
        onFailure: (Throwable) -> Unit = { e -> Log.e(TAG, "Failed to start UDP signaling", e) },
        onReady: () -> Unit,
    ) {
        release()
        listenerThread = UDPThread(
            socketTimeout = socketTimeout,
            onReady = onReady,
            onFailure = onFailure,
            onMessage = { address, message ->
                handleMessage(address, message)
            }
        )
        listenerThread?.start()
    }

    private fun handleMessage(
        senderAddress: InetAddress,
        message: String,
    ) {
        runCatching {
            // todo move ?
            val senderAddress = senderAddress.hostAddress ?: ""
            val isSenderAddress = senderAddress.isNotEmpty()
            val isRemoteIp = remoteIp.isNotEmpty()
            val isEqual = senderAddress.contentEquals(remoteIp)
            if (senderAddress.isEmpty()) return@runCatching
            if (isSenderAddress && isRemoteIp && isEqual.not()) {
                Log.w(TAG, "Remote address changed $remoteIp -> $senderAddress ! Omitting.")
                return@runCatching
            }
            remoteIp = senderAddress
            // todo, use kotlinx serialization
            val json = JSONObject(message)
            when (json.getString("type")) {
                "offer" -> onOfferReceived(json.getString("sdp"))
                "answer" -> onAnswerReceived(json.getString("sdp"))
                "candidate" -> runCatching {
                    onIceCandidateReceived(
                        IceCandidate(
                            json.getString("sdpMid"),
                            json.getInt("sdpMLineIndex"),
                            json.getString("candidate")
                        )
                    )
                }.onFailure { e ->
                    Log.e(TAG, "Failed to add ICE candidate", e)
                }

                "accept" -> onAcceptReceived()
                "dismiss" -> onDismissReceived(CallEndReason.REMOTE_PARTY_END)
            }

        }.onFailure { e ->
            Log.e(TAG, "Error parsing message", e)
        }
    }

    private inline fun <reified T : SDPMessage> sendMessage(
        sdpMessage: T,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) = scope.launch {
        Log.d(TAG, "Sending: $sdpMessage")
        runCatching {
            val message = sdpMessage.asJson<T>()
            val data = message.toByteArray()
            val address = InetAddress.getByName(remoteIp)
            val packet = DatagramPacket(data, data.size, address, signalingPort)
            listenerThread?.send(packet)
            Log.d(TAG, "Sent: ${message.take(100)}...")
        }.onFailure { e ->
            Log.e(TAG, "Error sending message", e)
        }
    }

    fun sendOffer(
        sdp: String
    ) = sendMessage(SDPOffer(sdp))

    fun sendAnswer(
        sdp: String
    ) = sendMessage(SDPAnswer(sdp))

    fun sendIceCandidate(
        candidate: IceCandidate
    ) = sendMessage(SDPIceCandidate(candidate))

    fun sendDismiss(
        sdp: String
    ) = sendMessage(SDPDismiss(sdp))

    fun sendAccept(
        sdp: String
    ) = sendMessage(SDPMessage.SDPAccept(sdp))

    fun release() {
        listenerThread?.release()
        listenerThread = null
    }
}
