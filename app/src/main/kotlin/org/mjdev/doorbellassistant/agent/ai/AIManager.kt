/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.ai

import android.content.Context
import org.mjdev.doorbellassistant.agent.ai.base.AiResult
import org.mjdev.doorbellassistant.agent.ai.base.IAiAgent
import org.mjdev.doorbellassistant.agent.ai.ollama.OllamaAgent
import org.mjdev.phone.helpers.DataBus

@Suppress("unused", "UNCHECKED_CAST", "CanBeParameter")
class AIManager(
    val context: Context,
    val agent: IAiAgent = OllamaAgent(),
    val configure: AIManager.() -> Unit = {}
) : DataBus<AiResult>(
    config = configure as DataBus<AiResult>.() -> Unit
) {
    fun transcript(
        text: String,
        onError: (Throwable) -> Unit = {},
        onResult: (String) -> Unit = { result ->
            send(AiResult.Response(result, ""))
        }
    ) {
        runCatching {
            agent.call(text, onError, onResult)
        }.onFailure { e ->
            send(AiResult.Error(e))
        }
    }

    fun init() {
        runCatching {
            agent.init()
        }.onFailure { e ->
            send(AiResult.Error(e))
        }
    }

    fun release() {
        runCatching {
            agent.release()
        }.onFailure { e ->
            send(AiResult.Error(e))
        }
    }

    companion object {
        val TAG = AIManager::class.simpleName
    }
}
