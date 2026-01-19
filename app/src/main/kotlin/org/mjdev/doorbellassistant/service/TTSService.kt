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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import org.mjdev.doorbellassistant.agent.tts.PiperTTSEngine
import org.mjdev.phone.service.BindableService

class TTSService : BindableService() {
    val tts by lazy {
        PiperTTSEngine(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        tts.initialize()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.release()
    }

    fun talk(text: String) {
        tts.talk(text)
    }

    companion object {
        @Composable
        fun rememberTTSService(
            context: Context = LocalContext.current
        ): State<TTSService?> = produceState(null, context) {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName,
                    binder: IBinder
                ) {
                    value = (binder as LocalBinder).service as TTSService?
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    value = null
                }
            }
            context.bindService(
                Intent(
                    context,
                    TTSService::class.java
                ),
                connection,
                BIND_AUTO_CREATE
            )
            awaitDispose {
                context.unbindService(connection)
            }
        }
    }
}
