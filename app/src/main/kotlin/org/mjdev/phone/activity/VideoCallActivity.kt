package org.mjdev.phone.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.phone.activity.base.UnlockedActivity
import org.mjdev.phone.rpc.NsdServerRpc.Companion.makeCall
import org.mjdev.phone.extensions.CustomExtensions.intent
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.helpers.ToolsJson.asJson
import org.mjdev.phone.helpers.ToolsJson.fromJson
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.service.NsdService
import org.mjdev.phone.service.CallNsdService.Companion.nsdDevice
import org.mjdev.phone.stream.CallEndReason
import org.mjdev.phone.ui.CallScreen

// todo speaker due type of device
@Suppress("unused")
class VideoCallActivity : UnlockedActivity() {
    private val callee: MutableState<NsdDevice?> = mutableStateOf(null)
    private val caller: MutableState<NsdDevice?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                onEndCall = { reason -> handleCallEnd(reason) }
            )
        }
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val calleeIn: NsdDevice? = intent.getStringExtra(CALLEE)?.fromJson()
        val callerIn: NsdDevice? = intent.getStringExtra(CALLER)?.fromJson()
        if (callee.value?.address != calleeIn?.address) {
            callee.value = calleeIn
        }
        if (caller.value?.address != callerIn?.address) {
            caller.value = callerIn
        }
    }

    private fun handleCallEnd(
        reason: CallEndReason
    ) {
        finish()
    }

    @Previews
    @Composable
    fun MainScreen(
        onEndCall: (CallEndReason) -> Unit = {}
    ) {
        CallScreen(
            modifier = Modifier
                .navigationBarsPadding()
                .displayCutoutPadding()
                .fillMaxSize(),
            callerDevice = callee.value,
            calleeDevice = caller.value,
            onEndCall = onEndCall
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
            nsdDevice(
                serviceClass = serviceClass
            ) { device ->
                CoroutineScope(Dispatchers.Default).launch {
                    if (callee != null) {
                        makeCall(caller, callee)
                    }
                    intent<VideoCallActivity> {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(CALLEE, callee?.asJson())
                        putExtra(CALLER, (caller ?: device)?.asJson())
                        startActivity(this@intent)
                    }
                }
            }
        }
    }
}
