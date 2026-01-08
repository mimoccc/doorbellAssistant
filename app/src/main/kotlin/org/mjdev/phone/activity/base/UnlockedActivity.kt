package org.mjdev.phone.activity.base

import org.mjdev.phone.extensions.CustomExtensions.dismissKeyguard
import org.mjdev.phone.extensions.CustomExtensions.turnDisplayOn

open class UnlockedActivity : BaseActivity() {
    override fun onResume() {
        super.onResume()
        dismissKeyguard()
        turnDisplayOn()
    }
}
