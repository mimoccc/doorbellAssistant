package org.mjdev.phone.rpc.plugins

import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.createApplicationPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object StopingPlugin {
    class StopPluginConfig(
        var onStop: () -> Unit = {}
    )

    @Suppress("DEPRECATION")
    val StopingPlugin = createApplicationPlugin(
        name = "StopingPlugin",
        createConfiguration = ::StopPluginConfig
    ) {
        application.environment.monitor.subscribe(ApplicationStopping) {
            CoroutineScope(Dispatchers.Main).launch {
                pluginConfig.onStop()
            }
        }
    }
}
