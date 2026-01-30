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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.mjdev.phone.helpers.json.ToolsJson.asJson
import org.mjdev.phone.helpers.json.ToolsJson.fromJson
import org.mjdev.phone.service.ServiceEvent.NotYetImplemented
import kotlin.reflect.KClass

@Suppress("unused")
open class RemoteBindableService : Service() {
    private val messengers = mutableListOf<Messenger>()

    private val messenger by lazy {
        Messenger(Handler(Looper.getMainLooper()) { msg ->
            onGotCommand(msg)
        })
    }

    @Suppress("DEPRECATION")
    open fun onGotCommand(msg: Message?): Boolean {
        val commandJson = msg?.data?.getSerializable(SERVICE_COMMAND) as? String
            ?: return true
        val command: ServiceCommand = commandJson.fromJson()
        messengers.add(msg.replyTo)
        executeCommand(command) { ev ->
            val result = ev.asJson()
            msg.replyTo?.let { messenger ->
                messenger.send(
                    Message.obtain(null, 0).apply {
                        data = bundleOf(SERVICE_RESPONSE to result)
                    }
                )
            }
        }
        return true
    }

    fun sendServiceEvent(event: ServiceEvent) {
        val result = event.asJson()
        messengers.forEach { messenger ->
            messenger.send(
                Message.obtain(null, 0).apply {
                    data = bundleOf(SERVICE_RESPONSE to result)
                }
            )
        }
        Log.d(this::class.simpleName, "Event send $event")
    }

    fun clearEvents() {
        // todo
    }

    open fun executeCommand(
        command: ServiceCommand,
        handler: (ServiceEvent) -> Unit
    ) = handler(NotYetImplemented)

    @CallSuper
    override fun onBind(intent: Intent): IBinder {
        return messenger.binder
    }

    open class ServiceConnector<T : RemoteBindableService>(
        private val context: Context,
        private val clazz: KClass<T>
    ) {
        private val replyHandler by lazy { ReplyHandler(this) }
        var serviceMessenger: Messenger? = null
        val events = mutableStateListOf<ServiceEvent>()
        private val connection = object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                binder: IBinder?
            ) {
                serviceMessenger = Messenger(binder)
            }

            override fun onServiceDisconnected(
                name: ComponentName?
            ) {
                serviceMessenger = null
            }
        }

        fun connect() {
            runCatching {
                Intent(context, clazz.java).also { intent ->
                    context.bindService(intent, connection, BIND_AUTO_CREATE)
                }
            }.onFailure { e ->
                e.printStackTrace()
            }
        }

        fun disconnect() {
            runCatching {
                context.unbindService(connection)
            }.onFailure { e ->
                e.printStackTrace()
            }
        }

        fun subscribe(
            handler: (ServiceEvent) -> Unit
        ) {
            events.forEach(handler)
        }

        inline fun <reified C : ServiceCommand> send(
            command: C
        ) = CoroutineScope(Dispatchers.Default).launch {
            val msg = Message.obtain(null, 0)
            val commandJson = command.asJson<C>()
            msg.replyTo = Messenger(ReplyHandler(this@ServiceConnector))
            msg.data = bundleOf(SERVICE_COMMAND to commandJson)
            while (serviceMessenger == null) {
                delay(100)
            }
            serviceMessenger?.send(msg)
        }

        class ReplyHandler(
            private val serviceConnector: ServiceConnector<*>
        ) : Handler(Looper.getMainLooper()) {
            @Suppress("DEPRECATION")
            override fun handleMessage(msg: Message) {
                val evJson = msg.data.getSerializable(SERVICE_RESPONSE) as? String
                val ev = evJson?.fromJson<ServiceEvent>()
                if (ev != null) {
                    // todo clear events
                    serviceConnector.events.add(ev)
                }
            }
        }
    }

    companion object {
        const val SERVICE_COMMAND = "service_command"
        const val SERVICE_RESPONSE = "service_response"

        @Composable
        fun <T : RemoteBindableService> rememberService(
            clazz: KClass<T>,
            context: Context = LocalContext.current
        ): State<ServiceConnector<T>?> {
            return produceState<ServiceConnector<T>?>(null) {
                val connector = ServiceConnector(context, clazz)
                connector.connect()
                value = connector
                awaitDispose {
                    connector.disconnect()
                }
            }
        }

        fun <T : RemoteBindableService> serviceFlow(
            context: Context,
            clazz: KClass<T>
        ): Flow<ServiceConnector<T>> = callbackFlow {
            val connector = ServiceConnector(context, clazz)
            connector.connect()
            trySend(connector)
            awaitClose {
                connector.disconnect()
            }
        }
    }
}
