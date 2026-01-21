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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.phone.activity.base.UnlockedActivity
import org.mjdev.phone.extensions.ActivityExt.turnDisplayOff
import org.mjdev.phone.extensions.ActivityExt.turnDisplayOn
import org.mjdev.phone.extensions.ContextExt.currentWifiIP
import org.mjdev.phone.extensions.ContextExt.intent
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.helpers.json.ToolsJson.asJson
import org.mjdev.phone.helpers.json.ToolsJson.fromJson
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.service.NsdService
import org.mjdev.phone.stream.CallEndReason
import org.mjdev.phone.ui.components.VideoCall
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.webrtc.SessionDescription

// todo speaker due type of device
@Suppress("unused")
open class VideoCallActivity : UnlockedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnDisplayOn()
        val callee: NsdDevice? = intent.getStringExtra(CALLEE)?.fromJson()
        val caller: NsdDevice? = intent.getStringExtra(CALLER)?.fromJson()
        setContent {
            MainScreen(
                caller = caller,
                callee = callee,
                onEndCall = { reason -> handleCallEnd(reason) }
            )
        }
    }

    private fun handleCallEnd(reason: CallEndReason) {
        turnDisplayOff()
        finish()
    }

    @Suppress("ParamsComparedByRef")
    @Previews
    @Composable
    fun MainScreen(
        onStartCall: (SessionDescription) -> Unit = {},
        onEndCall: (CallEndReason) -> Unit = {},
        caller: NsdDevice? = NsdDevice.EMPTY,
        callee: NsdDevice? = NsdDevice.EMPTY,
    ) = PhoneTheme {
        VideoCall(
            modifier = Modifier.fillMaxSize(),
            callee = callee ?: NsdDevice.EMPTY,
            caller = caller ?: NsdDevice.EMPTY,
            isCaller = caller?.address == currentWifiIP,
            onEndCall = onEndCall,
            onStartCall = onStartCall
        )
    }

    companion object {
        val packageName = VideoCallActivity::class.java.`package`?.name
            ?.replace(".activity", "")

        val CALLER = "$packageName.CALLER"
        val CALLEE = "$packageName.CALLEE"

        fun Context.startCall(
            serviceClass: Class<out NsdService>,
            callee: NsdDevice? = null,
            caller: NsdDevice? = null
        ) {
            intent<VideoCallActivity> {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(CALLEE, callee?.asJson())
                putExtra(CALLER, caller?.asJson())
                startActivity(this@intent)
            }
        }
    }
}
