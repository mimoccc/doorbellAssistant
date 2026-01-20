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

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

object OllamaModels {
    object PHI {
        val PHI_3_MINI: LLModel = LLModel(
            provider = LLMProvider.Ollama,
            id = "phi3:mini",
            capabilities = listOf(
                LLMCapability.Temperature,
                LLMCapability.Schema.JSON.Basic,
                LLMCapability.Tools
            ),
            contextLength = 131_072,
        )
        val PHI_3_MINI_INSTRUCT: LLModel = LLModel(
            provider = LLMProvider.Ollama,
            id = "phi3:mini-instruct",
            capabilities = listOf(
                LLMCapability.Temperature,
                LLMCapability.Schema.JSON.Basic,
                LLMCapability.Tools
            ),
            contextLength = 131_072,
        )
    }

    object Cloud {
        object QWEN3 {
            val QWEN3_CODER_480B_CLOUD: LLModel = LLModel(
                provider = LLMProvider.Ollama,
                id = "qwen3-coder:480b-cloud",
                capabilities = listOf(
                    LLMCapability.Temperature,
                    LLMCapability.Schema.JSON.Basic,
                    LLMCapability.Tools
                ),
                contextLength = 131_072,
            )
        }
        object GPT {
            val GPT_OSS_120B_CLOUD: LLModel = LLModel(
                provider = LLMProvider.Ollama,
                id = "gpt-oss:120b-cloud",
                capabilities = listOf(
                    LLMCapability.Temperature,
                    LLMCapability.Schema.JSON.Basic,
                    LLMCapability.Tools
                ),
                contextLength = 131_072,
            )
            val GPT_OSS_20B_CLOUD: LLModel = LLModel(
                provider = LLMProvider.Ollama,
                id = "gpt-oss:20b-cloud",
                capabilities = listOf(
                    LLMCapability.Temperature,
                    LLMCapability.Schema.JSON.Basic,
                    LLMCapability.Tools
                ),
                contextLength = 131_072,
            )
        }
    }
}
