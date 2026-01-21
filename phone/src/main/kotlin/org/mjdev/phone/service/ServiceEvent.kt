/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.service

import org.mjdev.phone.helpers.json.Serializable

@Serializable
open class ServiceEvent {
    @Serializable
    data class ServiceError(
        val error: Throwable
    ) : ServiceEvent()

    @Serializable
    object NotYetImplemented : ServiceEvent()

    @Serializable
    data class ServiceConnected(
        val address:String,
        val port:Int
    ) : ServiceEvent()

    @Serializable
    object ServiceDisconnected : ServiceEvent()
}