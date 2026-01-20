package org.mjdev.phone.rpc.routing

import io.ktor.server.routing.Routing
import org.mjdev.phone.rpc.server.NsdServerRpc
import org.mjdev.phone.rpc.action.NsdAction

interface NsdRouting : Routing {
    val nsdServerRpc: NsdServerRpc
    val onAction: (NsdAction) -> Unit
}