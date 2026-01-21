/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.ai.base

import org.mjdev.phone.helpers.json.Serializable

@Serializable
open class AiResult {

    @Serializable
    object Initialized : AiResult()

    @Serializable
    object Released : AiResult()

    @Serializable
    data class Error(
        val error: Throwable
    ) : AiResult()

    @Serializable
    data class Response(
        val data: String,
        val command: String
    ) : AiResult()

}
