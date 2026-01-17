package org.mjdev.doorbellassistant.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.agent.ai.AIManager.Companion.TAG
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKit
import org.mjdev.doorbellassistant.agent.stt.transcribers.vosk.VoskKit
import org.mjdev.doorbellassistant.extensions.ComposeExt.VisibleState
import org.mjdev.doorbellassistant.ui.components.VoiceRecognizerState.Companion.rememberWhisperVoiceRecognizerState
import org.mjdev.doorbellassistant.ui.theme.Red
import org.mjdev.doorbellassistant.ui.theme.White
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Suppress("unused")
@Previews
@SuppressLint("MissingPermission")
@Composable
fun AISpeechRecognizer(
    modifier: Modifier = Modifier,
    autoStart: Boolean = false,
    voiceDetectionSensitivity: Float = 0.2f,
    stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    maxRecordingDurationMs: Long = 20000L,
    minRecordingDurationMs: Long = 2000L,
    onVoiceRecognizerInitialized: (VoiceRecognizerState) -> Unit = {},
    onConversationEnds: () -> Unit = {},
    onDownloading: (Float) -> Unit = {},
    onVoiceDetected: () -> Unit = {},
    onVoiceUnDetected: () -> Unit = {},
    onConversationResponded: () -> Unit = {},
    onInterruptions: () -> Unit = {},
    onCommand: (String) -> Boolean = { false },
    onError: (Throwable) -> Unit = { e -> Log.e(TAG, "Error in ai.", e) },
    onThinking: () -> Unit = {},
    createKit: (Context) -> ITKit = { context -> VoskKit(context) },
) = PhoneTheme {
    val bckColor = phoneColors.colorLabelsBackground
    var isFirstText by remember { mutableStateOf(true) }
    var textState by remember { mutableStateOf("...") }
    val recognizerState = rememberWhisperVoiceRecognizerState(
        stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
        voiceDetectionSensitivity = voiceDetectionSensitivity,
        maxRecordingDurationMs = maxRecordingDurationMs,
        minRecordingDurationMs = minRecordingDurationMs,
        onInitialized = {
            textState = "Welcome..."
            onVoiceRecognizerInitialized(this)
        },
        onVoiceDetected = {
            onVoiceDetected()
        },
        onVoiceStarts = {
            onVoiceDetected()
        },
        onVoiceEnds = {
            stopListening()
            onVoiceUnDetected()
            // todo ai action
        },
        onDownloading = { percent ->
            textState = "Downloading model ${(percent * 100).toInt()} %."
            onDownloading(percent)
        },
        onThinking = {
            onThinking()
        },
        onVoiceTranscribed = { text, segments ->
            if (isFirstText) {
                isFirstText = false
                textState = text
            } else {
                textState += " $text"
            }
        },
        onFailure = { e ->
            Log.e(TAG, "Voice recognizer failed.", e)
            textState = "${e.message}"
            onFailure(e)
        },
    )
    val thinkingState by remember(recognizerState.isThinking, recognizerState.isListening) {
        derivedStateOf {
            recognizerState.isThinking || recognizerState.isListening
        }
    }
    val listenState by remember(recognizerState.isListening) {
        derivedStateOf {
            recognizerState.isListening
        }
    }
    val colorStateThinking by remember(thinkingState) {
        derivedStateOf {
            if (thinkingState) Red else bckColor
        }
    }
    val colorStateListening by remember(listenState) {
        derivedStateOf {
            if (listenState) Red else bckColor
        }
    }
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    colorStateThinking,
                    RoundedCornerShape(16.dp),
                )
                .padding(start = 58.dp, end = 12.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp, start = 10.dp),
                text = textState,
                minLines = 1,
                maxLines = 1,
                textAlign = TextAlign.Start,
                color = White
            )
            VisibleState(thinkingState) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopStart),
                    trackColor = Color.White,
                    color = Color.Transparent
                )
            }
            VisibleState(thinkingState) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.BottomStart),
                    trackColor = Color.White,
                    color = Color.Transparent
                )
            }
        }
        WhisperVoiceRecognizer(
            modifier = Modifier
                .size(64.dp)
                .background(
                    colorStateListening,
                    CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
                .padding(2.dp)
                .align(Alignment.CenterStart),
            state = recognizerState,
            autoStart = autoStart,
            voiceDetectionSensitivity = 0.2f,
            stopListeningWhenNoVoiceAtLeast = 2.0f,
            maxRecordingDurationMs = 20000L,
            minRecordingDurationMs = 2000L,
            createKit = createKit,
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            recognizerState.stopListening()
        }
    }
}
