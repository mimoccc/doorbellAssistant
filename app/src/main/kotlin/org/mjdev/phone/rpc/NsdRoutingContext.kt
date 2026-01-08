package org.mjdev.phone.rpc

import io.ktor.server.routing.Routing

class NsdRoutingContext(
    private val routing: Routing,
    override val nsdServerRpc: NsdServerRpc,
    override val onAction: (NsdAction) -> Unit
) : NsdRouting, Routing by routing