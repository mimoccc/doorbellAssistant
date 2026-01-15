package org.mjdev.phone.activity

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.phone.activity.VideoCallActivity.Companion.startCall
import org.mjdev.phone.activity.base.UnlockedActivity
import org.mjdev.phone.application.CallApplication.Companion.getCallServiceClass
import org.mjdev.phone.helpers.GlanceNotificationManager
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.service.CallNsdService.Companion.nsdDevice
import org.mjdev.phone.nsd.service.NsdService
import org.mjdev.phone.ui.components.NsdList
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Suppress("UNCHECKED_CAST")
open class IntercomActivity : UnlockedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
        // todo
//        GlanceNotificationManager(this).test()
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
                types = NsdTypes.entries,
                onError = { e -> Log.e(TAG, e.message, e) },
                onCallClick = { callee ->
                    val serviceClass = getCallServiceClass() as Class<NsdService>
                    nsdDevice { caller ->
                        startCall(
                            serviceClass = serviceClass,
                            callee = callee,
                            caller = caller
                        )
                    }
                }
            )
        }
    }

    companion object {
        private val TAG = IntercomActivity::class.simpleName
    }
}
