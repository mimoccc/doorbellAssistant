package org.mjdev.doorbellassistant.activity.base

import android.os.Bundle
import org.mjdev.doorbellassistant.extensions.ComposeExt.hideSystemBars
import org.mjdev.doorbellassistant.extensions.ComposeExt.setFullScreen

open class FullScreenActivity : UnlockedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        setFullScreen()
    }
}