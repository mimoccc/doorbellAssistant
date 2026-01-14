package org.mjdev.phone.rpc.plugins

import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object StartupPlugin {
    class StartupPluginConfig(
        var onStart: (address: String, port: Int) -> Unit = { _, _ -> }
    )

    @Suppress("DEPRECATION")
    val StartupPlugin = createApplicationPlugin(
        name = "StartupPlugin",
        createConfiguration = ::StartupPluginConfig
    ) {
        application.environment.monitor.subscribe(ApplicationStarted) {
            CoroutineScope(Dispatchers.IO).launch {
                val connectors = application.engine.resolvedConnectors()
                val firstConnector = connectors.first()
                val address = firstConnector.host
                val port = firstConnector.port
                withContext(Dispatchers.Main) {
                    pluginConfig.onStart(address, port)
                }
            }
        }
    }
}
