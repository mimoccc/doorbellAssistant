package org.mjdev.doorbellassistant.application

import android.app.Application
import android.util.Log
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.conscrypt.Conscrypt
import org.kodein.di.DIAware
import org.kodein.di.LazyDI
import org.mjdev.doorbellassistant.di.mainDI
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.phone.service.CallNsdService.Companion.start
import java.security.Security

class App : Application(), DIAware {
    override val di: LazyDI by mainDI(this@App)

    companion object {
        private val TAG = App::class.simpleName
        private const val TAG_CRASH = "Crash"
    }

    override fun onCreate() {
        super.onCreate()
        setupExceptionHandler()
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        Security.addProvider(BouncyCastleProvider())
        start<DoorbellNsdService>()
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
