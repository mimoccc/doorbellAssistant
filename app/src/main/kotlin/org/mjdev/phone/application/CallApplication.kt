package org.mjdev.phone.application

import android.app.Application
import android.content.Context
import android.content.Intent
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
        Intent(this, service).also { intent ->
            startForegroundService(intent)
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
