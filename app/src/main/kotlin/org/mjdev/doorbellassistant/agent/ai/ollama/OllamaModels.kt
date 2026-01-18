package org.mjdev.doorbellassistant.agent.ai.ollama

// todo

enum class OllamaModels(
    val model : String
) {
    META_LLAMA("llama3.2:latest"),
    META_LLAMA_3_2("meta-llama-3-2"),
    META_LLAMA_3_2_12B("meta-llama-3-2-12b"),
    META_LLAMA_3_2_7B("meta-llama-3-2-7b"),
    META_LLAMA_3_2_3B("meta-llama-3-2-3b"),
    META_LLAMA_3_2_1B("meta-llama-3-2-1b"),

    QWEN3_CODER_480B_CLOUD("qwen3-coder:480b-cloud"),

    GPT_OSS_20B_CLOUD("gpt-oss:20b-cloud"),
    GPT_OSS_120B_CLOUD("gpt-oss:120b-cloud"),
}
