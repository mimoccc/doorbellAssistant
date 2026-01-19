/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.mjdev.doorbellassistant.activity.AssistantActivity
import org.mjdev.doorbellassistant.activity.AssistantActivity.Companion.isAppSetAsHomeLauncher
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.service.MotionDetectionService
import org.mjdev.phone.extensions.ActivityExt.startOrResume
import org.mjdev.phone.extensions.ContextExt.startForeground

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startForeground<DoorbellNsdService>()
            if (context.isAppSetAsHomeLauncher) {
                MotionDetectionService.start(context)
                context.startOrResume<AssistantActivity>()
            }
        }
    }
}
