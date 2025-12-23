package org.mjdev.doorbellassistant.helpers

import android.os.Handler

@Suppress("DEPRECATION")
class DelayHandler(
    val timeout: Long = 5000L,
    val handler: Handler = Handler(),
    val block: () -> Unit
) {
    var isStarted: Boolean = false
        internal set
    private val runnable = Runnable {
        block()
    }

    fun start() {
        stop()
        isStarted = true
        handler.postDelayed(runnable, timeout)
    }

    fun stop() {
        isStarted = false
        handler.removeCallbacks(runnable)
    }

    fun restart() {
        if (isStarted) {
            stop()
        }
        start()
    }
}
