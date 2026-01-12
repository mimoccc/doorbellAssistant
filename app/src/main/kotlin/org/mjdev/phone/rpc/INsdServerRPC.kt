package org.mjdev.phone.rpc

import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.stream.ICallManager
import org.webrtc.IceCandidate

abstract class INsdServerRPC {
    abstract val isRunning: Boolean
    abstract val address: String
    abstract val port: Int

    abstract suspend fun start(onStarted: (String, Int) -> Unit = { a, p -> })
    abstract suspend fun stop(onStopped: () -> Unit = {})

    abstract fun sendIceCandidate(device: NsdDevice, candidate: IceCandidate)
    abstract fun sendAccept(device: NsdDevice, sdp: String)
    abstract fun sendDismiss(device: NsdDevice, sdp: String)
    abstract fun sendOffer(device: NsdDevice, sdp: String)
    abstract fun sendAnswer(device: NsdDevice, sdp: String)
    abstract fun sendCallStart(caller: NsdDevice, callee: NsdDevice)
    abstract fun sendCallStarted(caller: NsdDevice, callee: NsdDevice)

    abstract fun registerCallManager(callManager: ICallManager)
    abstract fun unregisterCallManager(callManager: ICallManager)

    abstract fun getCallManagers(): List<ICallManager>
}