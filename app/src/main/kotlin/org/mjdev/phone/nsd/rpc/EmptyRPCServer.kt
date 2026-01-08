package org.mjdev.phone.nsd.rpc

import android.content.Context

class EmptyRPCServer(
    context: Context
) : INsdServerRPC(context) {
    override var isRunning: Boolean = false
    override val address: String = "0.0.0.0"
    override val port: Int = 8888

    override suspend fun start(onStarted: (String, Int) -> Unit) {
        isRunning = true
    }

    override suspend fun stop(onStopped: () -> Unit) {
        isRunning = false
    }
}
