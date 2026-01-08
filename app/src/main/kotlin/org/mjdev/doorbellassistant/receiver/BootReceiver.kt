package org.mjdev.doorbellassistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.mjdev.doorbellassistant.vpn.AdVpnService
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.phone.service.CallNsdService.Companion.start

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.start<DoorbellNsdService>()
            context.start<DoorbellNsdService>()
            AdVpnService.checkStartVpnOnBoot(context)

//            if (context.isAssistantEnabled) {
//            MotionDetectionService.start(context)
//            AssistantActivity.startOrResume(context)
//            }
        }
    }
}
