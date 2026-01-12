package org.mjdev.phone.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.phone.activity.base.UnlockedActivity
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.extensions.CustomExtensions.intent
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.helpers.ToolsJson.asJson
import org.mjdev.phone.helpers.ToolsJson.fromJson
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
            CoroutineScope(Dispatchers.Default).launch {
                intent<VideoCallActivity> {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(
                        CALLEE,
                        callee?.asJson()
                    )
                    putExtra(
                        CALLER,
                        caller?.asJson()
                    )
                    startActivity(this@intent)
                }
            }
        }
    }
}
