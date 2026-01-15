package org.mjdev.doorbellassistant.helpers

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
