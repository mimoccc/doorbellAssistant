/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.rpc.plugins

import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.createApplicationPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.mjdev.phone.helpers.json.ToolsJson.fromJson
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
class PeerSignalingRegistryPlugin {

    data class PeerInfo(
        val peerId: String,
        val endpoint: String,
        val timestamp: Long
    )

    abstract class PeerSignalingService(
        val address: String,
        val port: Int
    ) {
        abstract suspend fun register(
            httpClient: HttpClient,
            config: PeerSignalingRegistryConfig,
            actualPort: Int,
            serviceAddress: String
        )

        abstract suspend fun unregister(
            httpClient: HttpClient,
            config: PeerSignalingRegistryConfig,
            serviceAddress: String
        )
    }

    class StaticService(
        address: String = "",
        port: Int = 8889
    ) : PeerSignalingService(address, port) {
        override suspend fun register(
            httpClient: HttpClient,
            config: PeerSignalingRegistryConfig,
            actualPort: Int,
            serviceAddress: String
        ) {
//            val publicIp = getPublicIp(httpClient)
//            val endpoint = "ws://$publicIp:$actualPort"
            while (currentCoroutineContext().isActive) {
                try {
                    httpClient.post("$serviceAddress/peers") {
                        contentType(ContentType.Application.Json)
                        val value : Map<String, Any> = mapOf(
                            "peerId" to config.peerId,
//                            "endpoint" to endpoint
                            "timestamp" to System.currentTimeMillis()
                        )
                        setBody(value)
                    }
                } catch (e: Exception) {
                    println("Registration failed: ${e.message}")
                }
                delay(config.announceInterval)
            }
        }

        override suspend fun unregister(
            httpClient: HttpClient,
            config: PeerSignalingRegistryConfig,
            serviceAddress: String
        ) {
            try {
                httpClient.delete("$serviceAddress/peers/${config.peerId}")
            } catch (e: Exception) {
                println("Unregister failed: ${e.message}")
            }
        }
    }

    class P2PService(
        address: String = "https://ipfs.io/api/v0",
        port: Int = 80
    ) : PeerSignalingService(address, port) {
        override suspend fun register(
            httpClient: HttpClient,
            config: PeerSignalingRegistryConfig,
            actualPort: Int,
            serviceAddress: String
        ) {
//            val publicIp = getPublicIp(httpClient)
//            val endpoint = "ws://$publicIp:$actualPort"
            val topic = "phone-signaling-peers"
            while (currentCoroutineContext().isActive) {
                try {
                    httpClient.post("$serviceAddress/pubsub/pub") {
                        parameter("arg", topic)
                        val value : Map<String, Any> = mapOf<String, Any>(
//                            put("peerId", config.peerId)
//                            put("endpoint", endpoint)
//                            put("timestamp", System.currentTimeMillis())
                        )
                        parameter("arg", value)
                    }
                } catch (e: Exception) {
                    println("P2P announce failed: ${e.message}")
                }
                delay(config.announceInterval)
            }
        }

        override suspend fun unregister(
            httpClient: HttpClient,
            config: PeerSignalingRegistryConfig,
            serviceAddress: String
        ) {
        }
    }

    class PeerSignalingRegistryConfig {
        var peerId: String = UUID.randomUUID().toString()
        var signalingPort: Int = 8889
        var announceInterval: Duration = 60.seconds
        var service: PeerSignalingService = P2PService()
    }

    companion object {
        private suspend fun getPublicIp(httpClient: HttpClient): String = try {
            httpClient.get("https://api.ipify.org?format=json")
                .body<JsonObject>()
                .get("ip")?.asString ?: getLocalIp()
        } catch (e: Exception) {
            e.printStackTrace()
            getLocalIp()
        }

        private fun getLocalIp(): String = NetworkInterface.getNetworkInterfaces()
            .asSequence()
            .flatMap { it.inetAddresses.asSequence() }
            .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
            ?.hostAddress ?: "127.0.0.1"

        suspend fun discoverPeers(
            service: PeerSignalingService
        ): List<PeerInfo>? {
            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    gson {
                        enableComplexMapKeySerialization()
                    }
                }
            }
            return runCatching {
                when (service) {
                    is StaticService -> {
                        client.get("${service.address}/peers").body<List<PeerInfo>>()
                    }

                    is P2PService -> {
                        val topic = "phone-signaling-peers"
                        client.get("${service.address}/pubsub/sub") {
                            parameter("arg", topic)
                            timeout { requestTimeoutMillis = 5000 }
                        }.body<String>()
                            .lines()
                            .mapNotNull { line ->
                                try {
                                    line.fromJson<PeerInfo>()
                                } catch (e: Exception) {
                                    null
                                }
                            }
                    }

                    else -> { null }
                }
            }.apply {
                client.close()
            }.getOrNull()
        }

        @Suppress("DEPRECATION")
        val PeerSignalingRegistryPlugin = createApplicationPlugin(
            name = "PeerSignalingRegistryPlugin",
            createConfiguration = ::PeerSignalingRegistryConfig
        ) {
            val httpClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    gson {
                        enableComplexMapKeySerialization()
                    }
                }
            }
            application.environment.monitor.subscribe(ApplicationStarted) {
                CoroutineScope(Dispatchers.IO).launch {
                    val actualPort = application.engine
                        .resolvedConnectors()
                        .firstOrNull()
                        ?.port ?: pluginConfig.signalingPort
                    pluginConfig.service.register(
                        httpClient = httpClient,
                        config = pluginConfig,
                        actualPort = actualPort,
                        serviceAddress = pluginConfig.service.address
                    )
                }
            }
            application.environment.monitor.subscribe(ApplicationStopped) {
                CoroutineScope(Dispatchers.IO)  .launch {
                    pluginConfig.service.unregister(
                        httpClient,
                        pluginConfig,
                        pluginConfig.service.address
                    )
                    httpClient.close()
                }
            }
        }
    }
}
