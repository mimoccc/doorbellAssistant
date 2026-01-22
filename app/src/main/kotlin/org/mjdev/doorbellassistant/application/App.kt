/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.application

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.conscrypt.Conscrypt
import org.kodein.di.DIAware
import org.kodein.di.LazyDI
import org.mjdev.doorbellassistant.di.mainDI
import org.mjdev.doorbellassistant.service.DoorbellNsdService
import org.mjdev.doorbellassistant.ui.theme.Controls
import org.mjdev.doorbellassistant.ui.theme.DarkMD5
import org.mjdev.doorbellassistant.ui.theme.Item
import org.mjdev.doorbellassistant.ui.theme.Label
import org.mjdev.doorbellassistant.ui.theme.White
import org.mjdev.phone.application.CallApplication
import org.mjdev.phone.ui.theme.base.phoneCustomizer
import java.security.Security

class App : CallApplication<DoorbellNsdService>(), DIAware {
    override var service = DoorbellNsdService::class.java
    override val di: LazyDI by mainDI(this@App)

    override fun onCreate() {
        super.onCreate()
//        setupExceptionHandler()
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        Security.addProvider(BouncyCastleProvider())
        phoneCustomizer {
            colorsLight.colorBackground = DarkMD5
            colorsDark.colorBackground = DarkMD5
            colorsLight.colorLabelText = Controls
            colorsDark.colorLabelText = Controls
            colorsLight.colorIconTint = White
            colorsDark.colorIconTint = White
            colorsLight.colorVideoControlsBackground = Controls
            colorsDark.colorVideoControlsBackground = Controls
            colorsLight.colorIconsBackground = Controls
            colorsDark.colorIconsBackground = Controls
            colorsLight.colorGlow = White
            colorsDark.colorGlow = White
            colorsLight.colorLabelsBackground = Item
            colorsDark.colorLabelsBackground = Item
            colorsLight.colorCallerIconBorder = Item
            colorsDark.colorCallerIconBorder = Item
            colorsLight.colorLabelText = Label
            colorsDark.colorLabelText = Label
        }
        // startForeground<DoorbellNsdService>() - moved to MainActivity.onStart()
//        startLockScreenService()
    }

    override fun onTerminate() {
        super.onTerminate()
//        stopService<STTService>()
//        stopService<TTSService>()
//        stopService<AIService>()
    }

//    private fun setupExceptionHandler() {
//        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
//        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
//            Log.e(TAG, "$TAG_CRASH: Uncaught exception in thread: ${thread.name}", throwable)
//            Log.e(TAG, "$TAG_CRASH: Exception: ${throwable.javaClass.name}: ${throwable.message}")
//            Log.e(TAG, "$TAG_CRASH: Stack trace:\n${throwable.stackTraceToString()}")
//            defaultHandler?.uncaughtException(thread, throwable)
//        }
//    }

    companion object {
        private val TAG = App::class.simpleName
        private const val TAG_CRASH = "Crash"
    }
}
