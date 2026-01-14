package org.mjdev.doorbellassistant.agent.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.OllamaModels
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OpenAiAgent(
    serverUrl: String = "http://192.168.1.2:11434",
    systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    model: LLModel = OllamaModels.Meta.LLAMA_3_2,
    tools: () -> List<Tool<*, String>> = { listOf(SayToUser) }
) {
    val scope = CoroutineScope(Dispatchers.Default)
    val allTools by lazy {
        tools()
    }
    val agent by lazy {
        AIAgent(
            promptExecutor = simpleOllamaAIExecutor(serverUrl),
            systemPrompt = systemPrompt,
            llmModel = model,
            temperature = 0.7,
            toolRegistry = ToolRegistry {
                allTools.forEach { t ->
                    tool(t)
                }
            },
            maxIterations = 30
        )
    }

    fun call(
        prompt: String = "Hello! How can you help me?",
        onResult: (String) -> Unit = { result -> Log.d(TAG, result) }
    ) = scope.launch {
        runCatching {
            agent.run(prompt)
        }.onFailure { e ->
            onResult(e.message ?: "An unknown error occurred.")
        }.getOrNull()?.let { result ->
            onResult(result)
        }
    }

    companion object {
        private val TAG = OpenAiAgent::class.simpleName
        private const val DEFAULT_SYSTEM_PROMPT =
            "You are a helpful assistant. Answer user questions concisely."
    }
}
