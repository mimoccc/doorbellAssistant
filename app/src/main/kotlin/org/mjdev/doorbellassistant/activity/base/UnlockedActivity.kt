package org.mjdev.doorbellassistant.activity.base

import android.os.Bundle
import org.mjdev.doorbellassistant.extensions.ComposeExt.dismissKeyguard

open class UnlockedActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dismissKeyguard()
    }
}