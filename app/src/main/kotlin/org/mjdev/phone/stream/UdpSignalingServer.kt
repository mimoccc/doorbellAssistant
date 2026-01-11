package org.mjdev.phone.stream

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.mjdev.phone.helpers.ToolsJson.asJson
import org.webrtc.IceCandidate
import java.net.DatagramPacket
import java.net.InetAddress

class UdpSignalingServer(
    private val port: Int = 8889,
    private val socketTimeout: Int = 30000,
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
    companion object {
        val TAG = UdpSignalingServer::class.simpleName
    }

    val listenerThread: UDPThread by lazy {
        UDPThread(
            signalingPort = port,
            socketTimeout = socketTimeout,
            onReady = onReady,
            onFailure = onFailure,
            onMessage = { address, port, message ->
                handleMessage(address, port, message)
            }
        )
    }

    private var remoteIp: String = ""

    fun start() {
        stop()
        listenerThread.start()
    }

    private fun handleMessage(
        senderAddress: InetAddress,
        senderPort: Int?,
        message: String,
    ) {
        runCatching {
            // todo move ?
            val senderAddress = senderAddress.hostAddress ?: ""
            val isSenderAddress = senderAddress.isNotEmpty()
            val isRemoteIp = remoteIp.isNotEmpty()
            val isEqual = senderAddress.contentEquals(remoteIp)
            val isNotEmpty = senderAddress.isNotEmpty()
            if (isNotEmpty && isSenderAddress && isRemoteIp && isEqual.not()) {
                Log.w(TAG, "Remote address changed $remoteIp -> $senderAddress ! Omitting.")
            } else {
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
            }
        }.onFailure { e ->
            Log.e(TAG, "Error parsing message", e)
        }
    }

    inline fun <reified T : SDPMessage> sendMessage(
        sdpMessage: T,
        address: String,
        port: Int,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) = scope.launch {
        Log.d(TAG, "Sending: $sdpMessage")
        runCatching {
            val message = sdpMessage.asJson<T>()
            val data = message.toByteArray()
            val address = InetAddress.getByName(address)
            listenerThread.send(DatagramPacket(data, data.size, address, port))
            Log.d(TAG, "Sent: ${message.take(100)}...")
        }.onFailure { e ->
            Log.e(TAG, "Error sending message", e)
        }
    }

    fun stop() {
        listenerThread.release()
    }
}