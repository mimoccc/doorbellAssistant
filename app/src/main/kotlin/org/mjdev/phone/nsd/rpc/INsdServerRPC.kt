package org.mjdev.phone.nsd.rpc

import android.content.Context

abstract class INsdServerRPC(
    val context: Context
) {
    abstract val isRunning: Boolean
    abstract val address : String
    abstract val port : Int

    abstract suspend fun start(onStarted: (String, Int) -> Unit = { a, p -> })
    abstract suspend fun stop(onStopped: () -> Unit = {})
}