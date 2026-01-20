/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.ai.ollama

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.llm.LLModel
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.agent.ai.base.IAiAgent
import org.mjdev.doorbellassistant.agent.ai.ollama.OllamaModels.PHI.PHI_3_MINI
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalCoroutinesApi::class)
class OllamaAgent(
    serverPort: Int = DEFAULT_OLLAMA_PORT,
    connectionTimeout: Long = 10000,
    systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    model: LLModel = PHI_3_MINI,
    tools: () -> List<Tool<*, String>> = { listOf(SayToUser) }
) : IAiAgent {
    val scope = CoroutineScope(Dispatchers.Default)
    val networkScanner by lazy {
        NetworkScanner(serverPort, connectionTimeout)
    }
    val server = flow {
        networkScanner.scanLocalNetworkForOllamaServers().also { ns ->
            emit(ns.firstOrNull())
        }
    }.shareIn(scope = scope, started = SharingStarted.Eagerly, replay = 1)
    val allTools by lazy {
        tools()
    }
    val agent: Flow<AIAgent<String, String>?> = server.mapLatest { serverUrl ->
        if (serverUrl != null) {
            AIAgent(
                promptExecutor = simpleOllamaAIExecutor(serverUrl),
                systemPrompt = systemPrompt,
                llmModel = model,
                temperature = 0.7,
                toolRegistry = ToolRegistry.Companion {
                    allTools.forEach { t ->
                        tool(t)
                    }
                },
                maxIterations = 30
            )
        } else null
    }

    override fun init() {
        scope.launch {
            server.collectLatest { ip ->
                Log.d(TAG, "Got server $ip")
            }
        }
    }

    override fun release() {
        // todo if needed
    }

    override fun call(
        prompt: String,
        onError: (Throwable) -> Unit,
        onResult: (String) -> Unit
    ) {
        scope.launch {
            runCatching {
                agent.first()?.run(prompt) ?: throw (OllamaException("No server found."))
            }.onFailure { e ->
                onResult(e.message ?: "An unknown error occurred.")
                onError(e)
            }.getOrNull()?.let { result ->
                onResult(result)
            }
        }
    }

    class OllamaException(
        message: String,
        cause: Throwable? = null
    ) : Exception(message, cause)

    class NetworkScanner(
        val serverPort: Int,
        val connectionTimeout: Long
    ) : ArrayList<String>() {
        companion object {
            private val TAG = NetworkScanner::class.simpleName
        }

        private val discoveredServers = ConcurrentHashMap<String, String>()
        private val httpClient by lazy {
            HttpClient(CIO) {
                install(HttpTimeout) {
                    connectTimeoutMillis = connectionTimeout
                    requestTimeoutMillis = connectionTimeout
                }
            }
        }

        suspend fun scanLocalNetworkForOllamaServers(): NetworkScanner {
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
                    } for Ollama servers on port $serverPort"
                )
                val jobs = (1..254).map { hostPart ->
                    CoroutineScope(Dispatchers.IO).async {
                        val targetIp = "$networkBase.$hostPart"
                        if (targetIp != localIp) {
                            checkOllamaServer(targetIp, serverPort)
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
            return this
        }

        @Suppress("HttpUrlsUsage")
        suspend fun checkOllamaServer(
            ip: String,
            port: Int
        ) = runCatching {
            val url = "http://$ip:$port/api/tags"
            val response = httpClient.get(url)
            if (response.status.value == 200) {
                val responseBody = response.bodyAsText()
                if (responseBody.contains("models") || responseBody.contains("name")) {
                    discoveredServers[ip] = "http://$ip:$port"
                    Log.d(TAG, "Found Ollama server at: $ip:$port")
                }
            }
        }.onFailure { e ->
            // Log.e(TAG, "${e.message}")
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
    }

    companion object {
        private val TAG = OllamaAgent::class.simpleName

        const val DEFAULT_OLLAMA_PORT = 11434
        const val DEFAULT_SYSTEM_PROMPT =
            "You are a helpful assistant. Answer user questions concisely."
    }
}