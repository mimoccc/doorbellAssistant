package org.mjdev.doorbellassistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.mjdev.doorbellassistant.activity.AssistantActivity
import org.mjdev.doorbellassistant.activity.AssistantActivity.Companion.isDoorBellAssistantEnabled
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.phone.extensions.CustomExtensions.startOrResume
import org.mjdev.phone.nsd.service.CallNsdService.Companion.start

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.start<DoorbellNsdService>()
            context.start<DoorbellNsdService>()
            if (context.isDoorBellAssistantEnabled) {
                MotionDetectionService.start(context)
                startOrResume<AssistantActivity>(context)
            }
        }
    }
}
