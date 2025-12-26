package org.mjdev.doorbellassistant.activity

import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.mjdev.doorbellassistant.activity.base.UnlockedActivity
import org.mjdev.doorbellassistant.extensions.ComposeExt.applyIf
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_CLIENT
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.doorbellassistant.ui.components.NsdList
import org.mjdev.doorbellassistant.ui.components.VideoCall

class IntercomActivity : UnlockedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
        DoorbellNsdService.start(this)
    }

    companion object {
        private val TAG = IntercomActivity::class.simpleName
    }

    @Previews
    @Composable
    fun MainScreen(
        context: Context = LocalContext.current
    ) {
        var callDevice by remember { mutableStateOf<NsdServiceInfo?>(null) }
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .applyIf(callDevice != null) {
                    displayCutoutPadding()
                }
                .fillMaxSize()
        ) {
            when (callDevice) {
                null -> NsdList(
                    modifier = Modifier.fillMaxSize(),
                    types = listOf(DOOR_BELL_ASSISTANT, DOOR_BELL_CLIENT),
                    onError = { e -> Log.e(TAG, e.message, e) },
                    onCallClick = { nsdDevice ->
                        callDevice = nsdDevice
                    },
                )

                else -> {
                    VideoCall(
                        modifier = Modifier.fillMaxSize(),
                        device = callDevice,
                        onStart = {
                            MotionDetectionService.stop(context)
                        },
                        onDismiss = {
                            callDevice = null
//                                if (context.isAssistantEnabled) {
//                                    MotionDetectionService.start(context)
//                                }
                        }
                    )
                }
            }
        }
    }
}
