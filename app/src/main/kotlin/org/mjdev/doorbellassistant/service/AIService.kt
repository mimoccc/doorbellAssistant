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

import org.mjdev.doorbellassistant.agent.ai.AIManager
import org.mjdev.doorbellassistant.agent.ai.ollama.OllamaAgent
import org.mjdev.phone.helpers.DataBus.Companion.subscribeWithEventBroadcast
import org.mjdev.phone.service.BindableService

class AIService : BindableService() {
    val agent by lazy { OllamaAgent() }
    val aiManager by lazy {
        AIManager(
            context = applicationContext,
            agent = agent
        ) {
            subscribeWithEventBroadcast(applicationContext)
//            subscribe { event ->
//                Log.d(TAG, "$event")
//                when (event) {
//                    is AiResult.Error -> {
//                    }
//                    is AiResult.Initialized -> {
//                    }
//                    is AiResult.Released -> {
//                    }
//                    is AiResult.Response -> {
//                    }
//                }
//            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        aiManager.init()
    }

    override fun onDestroy() {
        super.onDestroy()
        aiManager.release()
    }

    companion object {
        private val TAG = AIService::class.simpleName
    }
}
