/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.service

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.mjdev.doorbellassistant.agent.tts.PiperTTSEngine
import org.mjdev.phone.helpers.DataBus
import org.mjdev.phone.helpers.DataBus.Companion.subscribe
import org.mjdev.phone.helpers.json.Serializable
import org.mjdev.phone.service.RemoteBindableService
import org.mjdev.phone.service.ServiceCommand
import org.mjdev.phone.service.ServiceEvent

// todo queue
class TTSService : RemoteBindableService() {
    private val tts by lazy {
        PiperTTSEngine(applicationContext)
    }
    private val eventBus: DataBus<String> = DataBus {
        subscribe { text ->
            // todo no repeat?
//            if (lastText?.equals(text)?.not() ?: true) {
                talk(text)
//            }
        }
    }
    private var lastText: String? = null

    override fun onCreate() {
        super.onCreate()
        tts.initialize()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.release()
    }

    override fun executeCommand(
        command: ServiceCommand,
        handler: (ServiceEvent) -> Unit
    ) {
        super.executeCommand(command, handler)
        val command = command as? TalkCommand
        if (command != null) {
            eventBus.send(command.text)
        }
    }

    fun talk(text: String) {
        if (text.trim().isNotEmpty()) {
            lastText = text
            tts.talk(text)
        }
    }

    class TTSServiceConnector(
        context: Context,
    ) : ServiceConnector<TTSService>(context, TTSService::class) {
        fun talk(
            text: String
        ) = send(TalkCommand(text))
    }

    @Serializable
    data class TalkCommand(
        val text: String
    ) : ServiceCommand()

    companion object {
        @Composable
        fun rememberTTSService() = LocalContext.current.let { context ->
            remember(context) {
                callbackFlow {
                    val connector = TTSServiceConnector(context)
                    connector.connect()
                    send(connector)
                    awaitClose {
                        connector.disconnect()
                    }
                }
            }
        }.collectAsState(null)
    }
}
