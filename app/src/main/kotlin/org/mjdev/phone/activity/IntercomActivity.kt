package org.mjdev.phone.activity

import android.content.Intent
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
import org.mjdev.phone.activity.VideoCallActivity.Companion.startCall
import org.mjdev.phone.activity.base.UnlockedActivity
import org.mjdev.phone.exception.NsdException
import org.mjdev.phone.extensions.PermissionsExt.LaunchPermissions
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.service.NsdService
import org.mjdev.phone.ui.components.BackgroundLayout
import org.mjdev.phone.ui.NsdList

@Suppress("AssignedValueIsNeverRead", "UNCHECKED_CAST")
class IntercomActivity : UnlockedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
        runCatching {
            startNsdService()
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    fun startNsdService() {
        Intent(
            this,
            getServiceClass()
        ).also { intent ->
            startForegroundService(intent)
        }
    }

    private fun getServiceClass(): Class<NsdService> =
        service ?: throw (NsdException("Service for calls must be registered."))

    companion object {
        private val TAG = IntercomActivity::class.simpleName

        private var service: Class<NsdService>? = null

        fun registerService(service: NsdService) {
            this.service = service::class.java as Class<NsdService>
        }
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
                    types = NsdTypes.entries,
                    onError = { e -> Log.e(TAG, e.message, e) },
                    onCallClick = { nsdDevice ->
                        this@IntercomActivity.startCall(
                            serviceClass = getServiceClass(),
                            callee = nsdDevice
                        )
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
                BackgroundLayout(
                    modifier = Modifier.fillMaxSize()
                )
                // todo permissions screen
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
