/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.stream

import org.mjdev.phone.nsd.device.NsdDevice

interface ICallManager {
    fun handleOfferReceived(sdp: String)
    fun handleAnswerReceived(sdp: String)
    fun handleAcceptReceived(sdp:String)
    fun handleIceCandidate(sdpMid: String, sdpMLineIndex: Int, sdp: String)
    fun handleDismissReceived(reason: CallEndReason)
    fun handleCallStarted(caller: NsdDevice, callee: NsdDevice)
}
