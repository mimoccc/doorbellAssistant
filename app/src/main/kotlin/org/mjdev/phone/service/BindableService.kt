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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.mjdev.phone.helpers.DataBus
import org.mjdev.phone.helpers.DataBus.Companion.subscribe
import org.mjdev.phone.service.ServiceEvent.Companion.NotYetImplemented

@Suppress("unused")
open class BindableService : LifecycleService() {
    private val eventBus = DataBus<ServiceEvent>()
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        var service: BindableService = this@BindableService
    }

    fun sendEvent(event: ServiceEvent) {
        eventBus.send(event)
    }

    @CallSuper
    open fun executeCommand(
        command: ServiceCommand,
        handler: (ServiceEvent) -> Unit
    ) {
        handler(NotYetImplemented)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    companion object {
        fun Context.bindableServiceFlow(): Flow<ServiceEvent> = callbackFlow {
            var job: Job? = null
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    val service = (binder as LocalBinder).service
                    job = launch {
                        service.eventBus.subscribe { ev ->
                            trySend(ev)
                        }
                    }
                    trySend(ServiceEvent.Companion.ServiceConnected)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    trySend(ServiceEvent.Companion.ServiceDisconnected)
                }

                override fun onBindingDied(name: ComponentName) {
                    close(Exception("Binding died"))
                }
            }
            val intent = Intent(this@bindableServiceFlow, BindableService::class.java)
            if (!bindService(intent, connection, BIND_AUTO_CREATE)) {
                close(Exception("Bind failed"))
            }
            awaitClose {
                job?.cancel()
                unbindService(connection)
            }
        }

        fun Context.serviceState(
            handler: (ServiceEvent) -> Unit
        ) {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    val service = (binder as LocalBinder).service
                    service.executeCommand(ServiceCommand.Companion.GetState, handler)
                    unbindService(this)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    handler(ServiceEvent.Companion.ServiceDisconnected)
                }
            }
            bindService(
                Intent(
                    this,
                    BindableService::class.java
                ), connection, BIND_AUTO_CREATE
            )
        }
    }
}