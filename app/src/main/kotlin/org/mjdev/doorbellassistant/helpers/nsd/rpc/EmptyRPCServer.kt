package org.mjdev.doorbellassistant.helpers.nsd.rpc

import android.content.Context

class EmptyRPCServer(
    context: Context,
) : INsdServerRPC(
    context = context
) {
    override suspend fun start() {}
    override suspend fun stop() {}
}
