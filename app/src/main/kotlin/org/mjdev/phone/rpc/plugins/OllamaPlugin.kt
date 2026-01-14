package org.mjdev.phone.rpc.plugins

import android.net.Uri
import android.util.Log
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentHashMap
import androidx.core.net.toUri
import io.ktor.client.plugins.HttpTimeout

@Suppress("HttpUrlsUsage", "LocalVariableName")
object OllamaPlugin {

    class OllamaPluginConfig {
        val serverPort: Int = 11434
        var scanInterval: Long = 5 * 60 * 1000
        var connectionTimeout: Long = 2000
    }

    @Suppress("DEPRECATION")
    val OllamaPlugin = createApplicationPlugin(
        name = "OllamaPlugin",
        createConfiguration = ::OllamaPluginConfig
    ) {
        val TAG = "OllamaPlugin"
        val scanInterval = pluginConfig.scanInterval
        val scanner = NetworkScanner(pluginConfig)
        application.environment.monitor.subscribe(ApplicationStarted) {
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    try {
                        scanner.scanLocalNetworkForOllamaServers()
                        if (scanner.isEmpty()) {
                            delay(scanInterval)
                        } else {
                            scanner.checkMinOneServerAlive()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Network scan error: ${e.message}")
                        delay(scanInterval)
                    }
                }
            }
        }
    }

    class NetworkScanner(
        private val config: OllamaPluginConfig,
    ) : ArrayList<Uri>() {
        companion object {
            private val TAG = NetworkScanner::class.simpleName
        }

        private val discoveredServers = ConcurrentHashMap<String, Uri>()
        private val httpClient by lazy {
            HttpClient(CIO) {
                install(HttpTimeout) {
                    connectTimeoutMillis = config.connectionTimeout
                    requestTimeoutMillis = config.connectionTimeout
                }
            }
        }

        suspend fun scanLocalNetworkForOllamaServers() {
            val localIp = getLocalIpAddress()
            if (localIp == null) {
                Log.e(TAG, "Could not determine local IP address.")
            } else {
                val networkBase = getNetworkBase(localIp)
                val subnetMask = getSubnetMask(localIp)
                Log.d(
                    TAG, "Scanning network: ${
                        networkBase
                    }/${
                        subnetMask
                    } for Ollama servers on port ${
                        config.serverPort
                    }"
                )
                val jobs = (1..254).map { hostPart ->
                    CoroutineScope(Dispatchers.IO).async {
                        val targetIp = "$networkBase.$hostPart"
                        if (targetIp != localIp) {
                            checkOllamaServer(targetIp, config.serverPort)
                        }
                    }
                }
                jobs.awaitAll()
                synchronized(this) {
                    clear()
                    addAll(discoveredServers.values)
                }
                Log.d(TAG, "Found ${discoveredServers.size} Ollama servers:")
                discoveredServers.values.forEachIndexed { idx, uri ->
                    Log.d(TAG, "$idx. Server at:  $uri")
                }
            }
        }

        private suspend fun checkOllamaServer(
            ip: String,
            port: Int
        ) = runCatching {
            val url = "http://$ip:$port/api/tags"
            val response = httpClient.get(url)
            if (response.status.value == 200) {
                val responseBody = response.bodyAsText()
                if (responseBody.contains("models") || responseBody.contains("name")) {
                    val serverUri = "http://$ip:$port".toUri()
                    discoveredServers[ip] = serverUri
                    println("Found Ollama server at: $ip:$port")
                }
            }
        }.onFailure { e ->
            Log.e(TAG, "${e.message}")
            discoveredServers.remove(ip)
        }

        private fun getLocalIpAddress(): String? = runCatching {
            NetworkInterface.getNetworkInterfaces()
                .asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { a ->
                    !a.isLoopbackAddress && a.hostAddress?.startsWith("192.168.") == true
                }
                ?.hostAddress
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()

        private fun getNetworkBase(localIp: String): String = localIp
            .split(".")
            .let { parts ->
                if (parts.size == 4) "${parts[0]}.${parts[1]}.${parts[2]}"
                else "192.168.1"
            }

        @Suppress("unused")
        private fun getSubnetMask(
            localIp: String
        ): Int {
            // Simple heuristic - most home networks use /24
            return 24
        }

        fun checkMinOneServerAlive() {
            // todo
        }
    }
}