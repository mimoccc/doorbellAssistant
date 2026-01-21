/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.stt.transcribers.vosk

import org.mjdev.phone.helpers.json.Serializable

@Serializable
@Suppress("unused")
open class VoskSize(
    val id: String
) {

    @Serializable
    object  SMALL: VoskSize("small")

    @Serializable
    object BIG : VoskSize("big")

}
