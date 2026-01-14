package org.mjdev.phone.rpc.routing

import io.ktor.server.routing.Routing
import org.mjdev.phone.rpc.server.NsdServerRpc
import org.mjdev.phone.rpc.action.NsdAction

class NsdRoutingContext(
    private val routing: Routing,
    override val nsdServerRpc: NsdServerRpc,
    override val onAction: (NsdAction) -> Unit
) : NsdRouting, Routing by routing