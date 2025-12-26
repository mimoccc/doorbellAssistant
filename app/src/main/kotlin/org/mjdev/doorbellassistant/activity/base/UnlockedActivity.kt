package org.mjdev.doorbellassistant.activity.base

import org.mjdev.doorbellassistant.extensions.ComposeExt.dismissKeyguard
import org.mjdev.doorbellassistant.extensions.ComposeExt.turnDisplayOn

open class UnlockedActivity : BaseActivity() {
    override fun onResume() {
        super.onResume()
        dismissKeyguard()
        turnDisplayOn()
    }
}