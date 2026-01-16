package org.mjdev.doorbellassistant.ui.components

import android.R.attr.autoStart
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.mjdev.doorbellassistant.agent.ai.AIManager.Companion.TAG
import org.mjdev.doorbellassistant.ui.components.WhisperRecognizerState.Companion.rememberWhisperVoiceRecognizerState
import org.mjdev.doorbellassistant.ui.theme.Red
import org.mjdev.doorbellassistant.ui.theme.White
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Suppress("unused")
@Previews
@SuppressLint("MissingPermission")
@Composable
fun AISpeechRecognizer(
    modifier: Modifier = Modifier,
    autoStart: Boolean = false,
    voiceDetectionSensitivity: Float = 0.2f,
    stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    onVoiceRecognizerInitialized: (WhisperRecognizerState) -> Unit = {},
    onConversationEnds: () -> Unit = {},
    onDownloading: (Float) -> Unit = {},
    onVoiceDetected: () -> Unit = {},
    onVoiceUnDetected: () -> Unit = {},
    onConversationResponded: () -> Unit = {},
    onInterruptions: () -> Unit = {},
    onCommand: (String) -> Boolean = { false },
    onError: (Throwable) -> Unit = { e -> Log.e(TAG, "Error in ai.", e) }
) = PhoneTheme {
    var textState by remember { mutableStateOf("...") }
    val recognizerState = rememberWhisperVoiceRecognizerState(
        stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
        voiceDetectionSensitivity = voiceDetectionSensitivity,
        onInitialized = {
            Log.d(TAG, "Voice recognize initialized.")
            textState = "Ready..."
            onVoiceRecognizerInitialized(this)
        },
        onVoiceDetected = {
            Log.d(TAG, "Voice detected.")
            textState = "Listening..."
            onVoiceDetected()
        },
        onVoiceStarts = {
            Log.d(TAG, "Voice starts.")
            textState = "Listening..."
            onVoiceDetected()
        },
        onVoiceEnds = {
            textState = "Ready..."
            onVoiceUnDetected()
        },
        onDownloading = { percent ->
            Log.d(TAG, "Voice recognizer downloading model.")
            textState = "Downloading model ${(percent * 100).toInt()} %."
            onDownloading(percent)
        },
        onFailure = { e ->
            Log.e(TAG, "Voice recognizer failed.", e)
            textState = "${e.message}"
        },
    )
    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                if (recognizerState.isListening) Red
                else White.copy(alpha = 0.3f),
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
            autoStart = autoStart,
            voiceDetectionSensitivity = 0.2f,
            stopListeningWhenNoVoiceAtLeast = 2.0f,
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
    DisposableEffect(Unit) {
        onDispose {
            recognizerState.stopListening()
        }
    }
}
