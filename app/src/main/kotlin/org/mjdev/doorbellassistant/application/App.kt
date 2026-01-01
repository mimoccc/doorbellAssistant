package org.mjdev.doorbellassistant.application

import android.app.Application
import android.util.Log
import org.kodein.di.DIAware
import org.kodein.di.LazyDI
import org.mjdev.doorbellassistant.di.mainDI
import org.mjdev.doorbellassistant.service.DoorbellNsdService

class App : Application(), DIAware {
    override val di: LazyDI by mainDI(this@App)

    companion object {
        private val TAG = App::class.simpleName
        private const val TAG_CRASH = "Crash"
    }

    override fun onCreate() {
        super.onCreate()
        setupExceptionHandler()
        DoorbellNsdService.start(this)
    }

    private fun setupExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "$TAG_CRASH: Uncaught exception in thread: ${thread.name}", throwable)
            Log.e(TAG, "$TAG_CRASH: Exception: ${throwable.javaClass.name}: ${throwable.message}")
            Log.e(TAG, "$TAG_CRASH: Stack trace:\n${throwable.stackTraceToString()}")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
