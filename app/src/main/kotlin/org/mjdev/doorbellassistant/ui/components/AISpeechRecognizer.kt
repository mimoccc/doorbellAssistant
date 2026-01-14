package org.mjdev.doorbellassistant.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.agent.ai.AIManager.Companion.TAG
import org.mjdev.doorbellassistant.ui.components.WhisperRecognizerState.Companion.rememberOboeRecognizerState
import org.mjdev.doorbellassistant.ui.theme.Red
import org.mjdev.doorbellassistant.ui.theme.White
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Suppress("unused")
@Previews
@SuppressLint("MissingPermission")
@Composable
fun AISpeechRecognizer(
    modifier: Modifier = Modifier,
    speechState: MutableState<Boolean> = mutableStateOf(isPreview),
    onConversationEnds: () -> Unit = {},
    onVoiceDetected: () -> Unit = {},
    onConversationResponded: (String) -> Unit = {},
    onInterruptions: () -> Unit = {},
    onCommand: (String) -> Boolean = { false },
    onError: (Throwable) -> Unit = { e -> Log.e(TAG, "Error in ai.", e) }
) = PhoneTheme {
//    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
//    val aiManager = rememberAiManager(
//        onCommand = onCommand,
//        onConversationEnds = onConversationEnds,
//        onConversationResponded = onConversationResponded,
//        onError = onError,
//    )
    var textState by remember { mutableStateOf("...") }
    val recognizerState = rememberOboeRecognizerState(
        onDownloading = { percent ->
            Log.d(TAG, "Voice recognizer downloading model.")
            textState = "Downloading model ${(percent * 100).toInt()} %."
        },
        onInitialized = {
            Log.d(TAG, "Voice recognize initialized.")
            textState = "Ready."
        },
        onVoiceStarts = {
            Log.d(TAG, "Voice starts.")
            textState = "Listening..."
        },
        onVoiceDetected = {
            Log.d(TAG, "Voice detected.")
            textState = "Speaking..."
        },
        onFailure = { e ->
            Log.e(TAG, "Voice recognizer failed.", e)
            textState = "${e.message}"
        }
    )
    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                if (speechState.value) Red else White.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp),
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WhisperVoiceRecognizer(
            modifier = Modifier
                .padding(2.dp)
                .size(32.dp)
                .background(
                    White.copy(alpha = 0.5f),
                    CircleShape
                )
                .padding(2.dp),
            state = recognizerState,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            text = textState,
            textAlign = TextAlign.Start,
            color = White
        )
    }
    LaunchedEffect(speechState.value) {
        if (speechState.value) {
            recognizerState.startListen()
        }
//        aiManager.onInterrupt = {
//            onInterruptions()
//        }
//        aiManager.onVoiceDetected = {
//            onVoiceDetected()
//        }
//        aiManager.onConversationResponded = { text ->
//            onConversationResponded(text)
//        }
//        aiManager.onConversationEnds = {
//            onConversationEnds()
//        }
//        aiManager.onCommand = { cmd ->
//            onCommand(cmd)
//        }
//        when (speechState.value) {
//            true -> {
//                if (micPermissionState.status.isGranted.not()) {
//                    micPermissionState.launchPermissionRequest()
//                }
//                aiManager.startConversation()
//            }
//
//            false -> aiManager.stopConversation()
//        }
    }
    DisposableEffect(speechState.value) {
        onDispose {
            CoroutineScope(Dispatchers.IO).launch {
                recognizerState.stopListening()
            }
//            aiManager.stopConversation()
        }
    }
}
