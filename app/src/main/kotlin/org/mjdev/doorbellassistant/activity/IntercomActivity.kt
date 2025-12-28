package org.mjdev.doorbellassistant.activity

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.activity.VideoCallActivity.Companion.startCall
import org.mjdev.doorbellassistant.activity.base.UnlockedActivity
import org.mjdev.doorbellassistant.extensions.ComposeExt.ANDROID_ID
import org.mjdev.doorbellassistant.extensions.ComposeExt.LaunchPermissions
import org.mjdev.doorbellassistant.extensions.ComposeExt.currentWifiIP
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.nsd.device.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.nsd.device.NsdTypes.DOOR_BELL_CLIENT
import org.mjdev.doorbellassistant.rpc.DoorBellAssistantServerRpc.Companion.makeCall
import org.mjdev.doorbellassistant.rpc.DoorBellAssistantServerRpc.Companion.sendMotionUnDetected
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.ui.components.NsdList

@Suppress("AssignedValueIsNeverRead")
class IntercomActivity : UnlockedActivity() {
    companion object {
        private val TAG = IntercomActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
        DoorbellNsdService.start(this)
    }

    @Previews
    @Composable
    fun MainScreen() {
        var arePermissionsGranted by remember { mutableStateOf(false) }
        if (arePermissionsGranted) {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxSize()
            ) {
                NsdList(
                    modifier = Modifier.fillMaxSize(),
                    types = listOf(DOOR_BELL_ASSISTANT, DOOR_BELL_CLIENT),
                    onError = { e -> Log.e(TAG, e.message, e) },
                    onCallClick = { nsdDevice ->
                        this@IntercomActivity.startCall(nsdDevice)
                    },
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .displayCutoutPadding()
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // permissions
            }
            LaunchPermissions(
                onPermissionsResult = { pms ->
                    arePermissionsGranted = pms.any { p -> p.value }
                    if (arePermissionsGranted.not()) recreate()
                },
                onAllPermissionsGranted = {
                    arePermissionsGranted = true
                }
            )
        }
    }
}
