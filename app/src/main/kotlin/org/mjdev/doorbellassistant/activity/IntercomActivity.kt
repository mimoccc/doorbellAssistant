package org.mjdev.doorbellassistant.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import org.mjdev.doorbellassistant.activity.base.UnlockedActivity
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_CLIENT
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.ui.components.NsdList

class IntercomActivity : UnlockedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NsdList(
                modifier = Modifier
                    .navigationBarsPadding()
                    .displayCutoutPadding()
                    .fillMaxSize(),
                types = listOf(DOOR_BELL_ASSISTANT, DOOR_BELL_CLIENT),
                onError = { e -> Log.e(TAG, e.message, e) },
                onClick = { nsdDevice ->

                },
            )
        }
        DoorbellNsdService.start(this)
    }

    companion object {
        private val TAG = IntercomActivity::class.simpleName
    }
}
