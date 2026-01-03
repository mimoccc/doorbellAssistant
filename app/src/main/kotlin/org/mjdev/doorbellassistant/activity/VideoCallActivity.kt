package org.mjdev.doorbellassistant.activity

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
import org.mjdev.doorbellassistant.BuildConfig
import org.mjdev.doorbellassistant.activity.base.UnlockedActivity
import org.mjdev.doorbellassistant.extensions.ComposeExt.ANDROID_ID
import org.mjdev.doorbellassistant.extensions.ComposeExt.asJson
import org.mjdev.doorbellassistant.extensions.ComposeExt.currentWifiIP
import org.mjdev.doorbellassistant.extensions.ComposeExt.fromJson
import org.mjdev.doorbellassistant.extensions.ComposeExt.intent
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.nsd.device.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.rpc.DoorBellAssistantServerRpc.Companion.makeCall
import org.mjdev.doorbellassistant.stream.CallEndReason
import org.mjdev.doorbellassistant.ui.components.CallScreen

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
    )  {
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
        const val CALLER = BuildConfig.APPLICATION_ID + ".CALLER"
        const val CALLEE = BuildConfig.APPLICATION_ID + ".CALLEE"

        fun Context.startCall(
            callee: NsdDevice? = null,
            caller: NsdDevice? = null
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                NsdDevice.fromData(
                    address = currentWifiIP,
                    serviceType = DOOR_BELL_ASSISTANT,
                    serviceName = ANDROID_ID
                ).also { callerIam ->
                    if (callee != null) {
                        makeCall(caller, callee)
                    }
                    intent<VideoCallActivity> {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(CALLEE, callee?.asJson())
                        putExtra(CALLER, (caller ?: callerIam).asJson())
                        startActivity(this@intent)
                    }
                }
            }
        }
    }
}
