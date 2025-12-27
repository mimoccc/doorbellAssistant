package org.mjdev.doorbellassistant.helpers.nsd.rpc;

import android.content.Context

abstract class INsdServerRPC(
    val context: Context,
    val port: Int = 8888,
) {
    abstract suspend fun start()
    abstract suspend fun stop()
}