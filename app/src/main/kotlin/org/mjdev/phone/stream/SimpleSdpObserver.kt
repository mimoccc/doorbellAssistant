package org.mjdev.phone.stream

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class SimpleSdpObserver : SdpObserver {
    override fun onSetSuccess() {
    }

    override fun onCreateSuccess(sdp: SessionDescription) {
    }

    override fun onSetFailure(error: String) {
    }

    override fun onCreateFailure(error: String) {
    }
}