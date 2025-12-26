package org.mjdev.doorbellassistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.mjdev.doorbellassistant.service.DoorbellNsdService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            DoorbellNsdService.start(context)
//            if (context.isAssistantEnabled) {
//            MotionDetectionService.start(context)
//            AssistantActivity.startOrResume(context)
//            }
        }
    }
}
