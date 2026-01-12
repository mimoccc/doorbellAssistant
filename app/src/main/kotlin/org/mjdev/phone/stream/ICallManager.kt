package org.mjdev.phone.stream

import org.mjdev.phone.nsd.device.NsdDevice

interface ICallManager {
    fun handleOfferReceived(sdp: String)
    fun handleAnswerReceived(sdp: String)
    fun handleAcceptReceived(sdp:String)
    fun handleIceCandidate(sdpMid: String?, sdpMLineIndex: Int, sdp: String?)
    fun handleDismissReceived(reason: CallEndReason)
    fun handleCallStarted(caller: NsdDevice?, callee: NsdDevice?)
}
