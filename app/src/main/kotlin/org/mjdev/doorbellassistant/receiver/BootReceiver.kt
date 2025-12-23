package org.mjdev.doorbellassistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.mjdev.doorbellassistant.activity.AssistantActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        AssistantActivity.startOrResume(context)
    }
}
