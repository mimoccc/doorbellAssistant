/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.extensions

import android.app.Activity
import android.content.Context
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import org.mjdev.phone.R

object WakeLockExt {
    private val TAG_WAKE_LOCK: Int = R.string.wake_lock

    var Activity.wakeLock: WakeLock?
        get() = runCatching {
            window.decorView.getTag(TAG_WAKE_LOCK) as? WakeLock
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull()
        set(value) {
            window.decorView.setTag(TAG_WAKE_LOCK, value)
        }

    fun Activity.acquireWakeLock() = runCatching {
        dismissWakeLock()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        this.wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MotionLauncher::AlertWakeLock"
        )
        wakeLock?.acquire(5_000)
    }.onFailure { e ->
        e.printStackTrace()
    }

    fun Activity.dismissWakeLock() {
        runCatching {
            this.wakeLock?.release()
        }.onFailure { e ->
            e.printStackTrace()
        }
        this.wakeLock = null
    }
}
