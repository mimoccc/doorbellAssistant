/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.annotation.CallSuper
import org.mjdev.phone.nsd.service.NsdService
import org.mjdev.phone.service.ServiceEvent.NotYetImplemented
import org.mjdev.phone.service.ServiceEvent.ServiceConnected
import org.mjdev.phone.service.ServiceEvent.ServiceDisconnected

@Suppress("unused")
open class LocalBindableService : Service() {
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        var service: LocalBindableService = this@LocalBindableService
    }

    @CallSuper
    open fun executeCommand(
        command: ServiceCommand,
        handler: (ServiceEvent) -> Unit
    ) {
        handler(NotYetImplemented)
    }

    @CallSuper
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    companion object {
       inline fun <reified T: NsdService> Context.serviceState(
           noinline handler: (ServiceEvent) -> Unit
        ) {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    val service = (binder as LocalBinder).service as NsdService
                    handler(ServiceConnected(service.address, service.port))
                    service.executeCommand(ServiceCommand.GetState) { ev ->
                        handler(ev)
                        unbindService(this)
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    handler(ServiceDisconnected)
                    unbindService(this)
                }
            }
            bindService(
                Intent(
                    this,
                    T::class.java
                ), connection, BIND_AUTO_CREATE
            )
        }
    }
}
