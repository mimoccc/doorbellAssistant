package org.mjdev.phone.activity.base

import android.os.Bundle
import org.mjdev.phone.extensions.CustomExtensions.hideSystemBars
import org.mjdev.phone.extensions.CustomExtensions.setFullScreen

open class FullScreenActivity : UnlockedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        setFullScreen()
    }
}
