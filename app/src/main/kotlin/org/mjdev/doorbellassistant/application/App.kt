package org.mjdev.doorbellassistant.application

import android.app.Application
import android.content.Context
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.ui.window.ComposeFloatingWindow
import org.mjdev.doorbellassistant.ui.window.ComposeFloatingWindow.Companion.alertLayoutParams

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DoorbellNsdService.start(this)
    }
}
