package org.mjdev.doorbellassistant.nsd.rpc

import android.content.Context

abstract class INsdServerRPC(
    val context: Context
) {
    abstract suspend fun start()
    abstract suspend fun stop()
}