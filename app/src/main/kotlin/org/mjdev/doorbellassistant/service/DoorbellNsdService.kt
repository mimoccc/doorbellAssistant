package org.mjdev.doorbellassistant.service

import android.content.Context
import android.content.Intent
import android.os.Build
import org.mjdev.doorbellassistant.activity.AssistantActivity
import org.mjdev.doorbellassistant.activity.base.BaseActivity.Companion.isRunning
import org.mjdev.doorbellassistant.helpers.nsd.NsdService
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_CLIENT

// todo automatic user login with wifi access
class DoorbellNsdService : NsdService() {
    override val port: Int = 8888

    override val serviceType: NsdTypes
        get() = if (baseContext.isRunning<AssistantActivity>()) DOOR_BELL_ASSISTANT
        else DOOR_BELL_CLIENT

    companion object {
        fun start(
            context: Context
        ) = runCatching {
            val intent = Intent(context, DoorbellNsdService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }

        fun restart(
            context: Context
        ) {
            stop(context)
            start(context)
        }

        fun stop(
            context: Context
        ) = runCatching {
            Intent(context, DoorbellNsdService::class.java).also { intent ->
                context.stopService(intent)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
    }
}
