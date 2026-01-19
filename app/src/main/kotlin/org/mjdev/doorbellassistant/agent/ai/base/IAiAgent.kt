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

interface IAiAgent {
    fun init()

    fun release()

    fun call(
        prompt: String,
        onError: (Throwable) -> Unit,
        onResult: (String) -> Unit
    )
}