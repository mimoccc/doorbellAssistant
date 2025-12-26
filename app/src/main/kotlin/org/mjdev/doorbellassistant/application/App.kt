package org.mjdev.doorbellassistant.application

import android.app.Application
import org.mjdev.doorbellassistant.service.DoorbellNsdService

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DoorbellNsdService.start(this)
    }
}