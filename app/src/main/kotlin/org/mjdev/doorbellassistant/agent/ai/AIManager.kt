package org.mjdev.doorbellassistant.agent.ai

import android.annotation.SuppressLint
import android.content.Context
//import android.speech.tts.Voice
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
//import com.google.firebase.Firebase
//import com.google.firebase.ai.ai
//import com.google.firebase.ai.type.GenerativeBackend
//import com.google.firebase.ai.type.LiveSession
//import com.google.firebase.ai.type.PublicPreviewAPI
//import com.google.firebase.ai.type.ResponseModality
//import com.google.firebase.ai.type.SpeechConfig
//import com.google.firebase.ai.type.Tool
//import com.google.firebase.ai.type.Transcription
//import com.google.firebase.ai.type.Voice
//import com.google.firebase.ai.type.content
//import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.R

// todo ai states, to keep conversation and end conversation
// todo actions
@Suppress("unused")
//@OptIn(PublicPreviewAPI::class)
class AIManager(
    val context: Context,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val modelName: String = context.getString(R.string.ai_model),
    val voiceName: String = context.getString(R.string.ai_voice_model),
    val systemPrompt: String = context.getString(R.string.ai_system_prompt),
//    val tools: List<Tool> = listOf(),
    var onConversationEnds: () -> Unit = {},
    var onVoiceDetected: () -> Unit = {},
    var onInterrupt: () -> Unit = {},
    var onConversationResponded: (String) -> Unit = {},
    var onCommand: (String) -> Boolean = { false },
    var onError: (Throwable) -> Unit = { e -> Log.e(TAG, "Error in ai.", e) }
) {
    val aiAgent by lazy {
        OllamaAgent()
    }
//    val model by lazy {
//        runCatching {
//            Firebase.ai(
//                backend = GenerativeBackend.googleAI()
//            ).liveModel(
//                modelName = modelName,
//                generationConfig = liveGenerationConfig {
//                    responseModality = ResponseModality.AUDIO
//                    speechConfig = SpeechConfig(
//                        voice = Voice(voiceName),
//                    )
//                },
//                systemInstruction = content {
//                    text(systemPrompt)
//                },
//                tools = tools,
//            )
//        }.onFailure { e -> onError(e) }.getOrNull()
//    }
//
//    var session: LiveSession? = null

    @SuppressLint("MissingPermission")
    fun startConversation() = scope.launch {
        runCatching {
//            val m = model ?: throw IllegalStateException("Model not initialized")
//            session = m.connect().apply {/
//                startAudioConversation(
//                    enableInterruptions = true,
//                    transcriptHandler = { u, m ->
//                        Log.d(TAG, "Transcript received - User: ${u?.text}, Model: ${m?.text}")
//                        handleTranscript(u, m)
//                    },
//                )
//                scope.launch {
//                    runCatching {
            // error
//                        receive().collect { event ->
//                            Log.d(TAG, "Received event: $event")
//                        }
//                    }.onFailure { e ->
//                        onError(e)
//                    }
//                }
//            }
            throw RuntimeException("Do not use ai during tests.")
        }.onFailure { e -> onError(e) }
    }

//    private fun handleTranscript(
//        user: Transcription?,
//        model: Transcription?
//    ) = runCatching {
//        user?.let {
//            onVoiceDetected()
//        }
//        model?.let { t ->
//            onConversationResponded(t.text ?: "")
//            if (onCommand(t.text ?: "")) {
//                stopConversation()
//            }
//        }
//    }.onFailure { e -> onError(e) }

    fun stopConversation() = scope.launch {
//        runCatching {
//            session?.stopAudioConversation()
//        }.onFailure { e ->
//            onError(e)
//        }
//        session = null
        aiAgent.release()
    }

    companion object {
        val TAG = AIManager::class.simpleName

        @Composable
        fun rememberAiManager(
            onCommand: (String) -> Boolean = { false },
            onConversationEnds: () -> Unit = {},
            onConversationResponded: (String) -> Unit = {},
            onError: (Throwable) -> Unit = { e -> Log.e(TAG, "Error in ai.", e) }
        ) : AIManager {
            val context: Context = LocalContext.current
            return remember {
                AIManager(
                    context = context,
                    onCommand = onCommand,
                    onConversationEnds = onConversationEnds,
                    onConversationResponded = onConversationResponded,
                    onError = onError
                )
            }
        }
    }
}
