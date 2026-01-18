package org.mjdev.doorbellassistant.agent.ai

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.mjdev.doorbellassistant.agent.ai.base.IAiAgent
import org.mjdev.doorbellassistant.agent.ai.ollama.OllamaAgent

@Suppress("unused")
class AIManager(
    val context: Context,
    var onCommand: (String) -> Boolean = { false },
    var onError: (Throwable) -> Unit = { e -> Log.e(TAG, "Error in ai.", e) },
    val createAgent: (Context) -> IAiAgent = { OllamaAgent() }
) {
    val aiAgent by lazy {
        OllamaAgent()
    }

    fun transcript(
        text: String,
        onError: (Throwable) -> Unit = {},
        onResult: (String) -> Unit = { result ->
            Log.d(TAG, result)
        }
    ) = aiAgent.call(text, onError, onResult)

    companion object {
        val TAG = AIManager::class.simpleName

        @Composable
        fun rememberAiManager(
            onCommand: (String) -> Boolean = { false },
            onError: (Throwable) -> Unit = { e -> Log.e(TAG, "Error in ai.", e) }
        ): AIManager {
            val context: Context = LocalContext.current
            return remember {
                AIManager(
                    context = context,
                    onCommand = onCommand,
                    onError = onError
                )
            }
        }
    }
}
