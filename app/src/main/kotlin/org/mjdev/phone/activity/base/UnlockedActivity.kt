/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.activity.base

import org.mjdev.phone.extensions.ActivityExt.turnDisplayOn
import org.mjdev.phone.extensions.KeyGuardExt.dismissKeyguard

open class UnlockedActivity : BaseActivity() {
    override fun onResume() {
        super.onResume()
        dismissKeyguard()
        turnDisplayOn()
    }
}
