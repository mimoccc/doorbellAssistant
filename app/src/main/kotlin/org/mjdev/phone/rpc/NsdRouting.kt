package org.mjdev.phone.rpc

import io.ktor.server.routing.Routing

interface NsdRouting : Routing {
    val nsdServerRpc: NsdServerRpc
    val onAction: (NsdAction) -> Unit
}