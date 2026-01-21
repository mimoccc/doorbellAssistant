/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.stream

import org.mjdev.phone.helpers.json.Serializable

@Serializable
@Suppress("ClassName")
open class CallEndReason {
    @Serializable
    object REMOTE_PARTY_END : CallEndReason()

    @Serializable
    object LOCAL_END : CallEndReason()
}
