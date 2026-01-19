/*
 *
 *  * Copyright (c) Milan Jurkulák 2026.
 *
 *  * Contact:
 *  * e: mimoccc@gmail.com
 *  * e: mj@mjdev.org
 *  * w: https://mjdev.org
 *  * w: https://github.com/mimoccc
 *  * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 *
 *
 */

package org.mjdev.phone.helpers

import android.os.Handler

@Suppress("DEPRECATION")
class DelayHandler(
    val timeout: Long = 5000L,
    val handler: Handler = Handler(),
    val block: () -> Unit
) {
    private val runnable = Runnable {
        block()
    }

    fun start() {
        stop()
        handler.postDelayed(runnable, timeout)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }

    fun restart() {
        stop()
        start()
    }
}
