package org.mjdev.phone.stream

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.UUID

class UDPThread(
    val signalingPort: Int = 8889,
    val socketTimeout: Int = 1000,
    val onFailure: (Throwable) -> Unit = {},
    val onReady: () -> Unit = {},
    val onMessage: (address: InetAddress, message: String) -> Unit,
) : Thread(UUID.randomUUID().toString()), AutoCloseable {
    companion object {
        private val TAG = UDPThread::class.simpleName
    }

    private val buffer = ByteArray(8192)

    @Volatile
    private var socket: DatagramSocket? = null

    @Volatile
    var isRunning: Boolean = false
        private set

    override fun run() {
        try {
            socket = DatagramSocket(null).apply {
                reuseAddress = true
                broadcast = true
                soTimeout = socketTimeout
                bind(InetSocketAddress(signalingPort))
            }
            isRunning = true
            Log.d(TAG, "UDP signaling started on port $signalingPort")
            onReady()
            while (isRunning) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket?.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    Log.d(TAG, "Received: $message")
                    onMessage(packet.address, message)
                } catch (_: SocketTimeoutException) {
                    continue
                } catch (e: SocketException) {
                    if (isRunning) {
                        Log.e(TAG, "Socket exception", e)
                        onFailure(e)
                    }
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start UDP signaling", e)
            onFailure(e)
        } finally {
            closeSocket()
            Log.d(TAG, "Listener thread exiting")
        }
    }

    fun send(packet: DatagramPacket) {
        if (!isRunning) throw SocketException("Thread stopped.")
        socket?.send(packet) ?: throw SocketException("Socket not bound.")
    }

    private fun closeSocket() {
        socket?.close()
        socket = null
    }

    fun release() {
        isRunning = false
        closeSocket()
        interrupt()
        if (currentThread() != this) {
            join(5000)
        }
    }

    override fun close() = release()
}
