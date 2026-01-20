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

abstract class ServiceEvent {
    companion object {
        data class ServiceError(
            val error: Throwable
        ) : ServiceEvent()

        object NotYetImplemented : ServiceEvent()
        object ServiceConnected : ServiceEvent()
        object ServiceDisconnected : ServiceEvent()
        object ServiceAlive : ServiceEvent()
    }
}