package org.mjdev.phone.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("unused")
open class BindableService : LifecycleService() {
    private val _events = MutableSharedFlow<ServiceEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        var service: BindableService = this@BindableService
    }

    abstract class ServiceEvent

    data class ServiceError(
        val error: Throwable
    ) : ServiceEvent()

    object NotYetImplemented : ServiceEvent()

    abstract class ServiceCommand

    object GetState : ServiceCommand()

    object ServiceDisconnected : ServiceEvent()

    fun sendEvent(event: ServiceEvent) {
        CoroutineScope(Dispatchers.Main).launch {
            _events.emit(event)
        }
    }

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
                    job = launch { service.events.collectLatest { trySend(it) } }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    trySend(ServiceDisconnected)
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
            val command: ServiceCommand = GetState
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    val service = (binder as LocalBinder).service
                    service.executeCommand(command, handler)
                    unbindService(this)
                }

                override fun onServiceDisconnected(name: ComponentName) {}
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
