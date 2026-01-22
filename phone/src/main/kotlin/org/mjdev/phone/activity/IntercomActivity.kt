/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.activity

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.mjdev.phone.activity.VideoCallActivity.Companion.startCall
import org.mjdev.phone.activity.base.BaseActivity
import org.mjdev.phone.application.CallApplication.Companion.getCallServiceClass
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdType
import org.mjdev.phone.nsd.service.CallNsdService.Companion.nsdDevice
import org.mjdev.phone.nsd.service.NsdService
import org.mjdev.phone.ui.components.NsdList
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Suppress("UNCHECKED_CAST")
open class IntercomActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }

    @Previews
    @Composable
    fun MainScreen() = PhoneTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(phoneColors.colorBackground)
        ) {
            NsdList(
                modifier = Modifier.fillMaxSize(),
                types = NsdType.entries,
                onCallClick = remember {
                    { callee ->
                        val serviceClass = getCallServiceClass() as Class<NsdService>
                        nsdDevice { caller ->
                            startCall(
                                serviceClass = serviceClass,
                                callee = callee,
                                caller = caller
                            )
                        }
                    }
                }
            )
        }
    }
}
