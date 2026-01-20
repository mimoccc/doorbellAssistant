/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.application

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.mjdev.phone.nsd.service.NsdService

abstract class CallApplication<T : NsdService> : Application() {
    abstract var service: Class<T>

    fun getServiceClass(): Class<T> = service

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Starting call application ${this::class.simpleName}")
        startNsdService()
    }

    fun startNsdService() {
        Log.d(TAG, "Starting service ${service.simpleName}")
        try {
            Intent(this, service).also { intent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}", e)
            // Fallback to regular service start if foreground service fails
            try {
                Intent(this, service).also { intent ->
                    startService(intent)
                }
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed to start service entirely: ${fallbackEx.message}", fallbackEx)
            }
        }
    }

    companion object {
        private val TAG = CallApplication::class.simpleName

        @Suppress(
            "UNCHECKED_CAST",
            "UPPER_BOUND_VIOLATED_IN_TYPE_OPERATOR_OR_PARAMETER_BOUNDS_WARNING"
        )
        fun <T : Class<NsdService>> Context.getCallServiceClass(): T {
            val serviceClass = (applicationContext as CallApplication<T>).getServiceClass() as T
            return serviceClass
        }
    }
}
