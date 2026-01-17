package org.mjdev.doorbellassistant.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import org.mjdev.doorbellassistant.enums.VideoSources
import org.mjdev.doorbellassistant.agent.ai.AIManager.Companion.TAG
import org.mjdev.doorbellassistant.agent.stt.transcribers.vosk.VoskKit
import org.mjdev.doorbellassistant.ui.components.AISpeechRecognizer
import org.mjdev.doorbellassistant.ui.components.CartoonPlayer
import org.mjdev.doorbellassistant.ui.components.CartoonPlayerState
import org.mjdev.doorbellassistant.ui.components.CartoonPlayerState.Companion.rememberCartoonState
import org.mjdev.doorbellassistant.ui.components.FrontCameraPreview
import org.mjdev.doorbellassistant.ui.components.VoiceRecognizerState
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.components.BackgroundLayout
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Suppress("ParamsComparedByRef")
@OptIn(UnstableApi::class)
@Previews
@Composable
fun MotionAlertScreen(
    imageState: MutableState<Bitmap?> = mutableStateOf(null),
    videoState: CartoonPlayerState = rememberCartoonState(),
    voiceDetectionSensitivity: Float = 0.2f,
    stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    onWelcomeVideoFinished: () -> Unit = {},
    onConversationContinued: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onCommand: (String) -> Boolean = { false },
    onThinking: () -> Unit = {},
) = PhoneTheme {
    var voiceRecognizerState: VoiceRecognizerState? = null
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(phoneColors.colorBackground),
        contentAlignment = Alignment.BottomCenter
    ) {
        BackgroundLayout(
            modifier = Modifier.fillMaxSize()
        )
        CartoonPlayer(
            modifier = Modifier.fillMaxSize(),
            state = videoState,
            onPaused = {
                false
            },
            onResumed = {
                true
            },
            onVideoFinish = { p ->
                when (videoState.videoState.value) {
                    VideoSources.Unavailable -> {
                        onDismiss() // todo records ?
                    }

                    VideoSources.Warning -> {
                        videoState.reset()
                    }

                    VideoSources.Welcome -> {
                        videoState.idle()
                        // todo better state handling
                        if (voiceRecognizerState?.isInitialized == true) {
                            voiceRecognizerState?.startListen()
                            onWelcomeVideoFinished()
                        } else {
                            // todo ???
                        }
                    }

                    else -> {
                        // ???
                    }
                }
            }
        )
        AISpeechRecognizer(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            voiceDetectionSensitivity = voiceDetectionSensitivity,
            stopListeningWhenNoVoiceAtLeast = stopListeningWhenNoVoiceAtLeast,
            onVoiceRecognizerInitialized = { state ->
                voiceRecognizerState = state
            },
            onConversationResponded = onConversationContinued,
            onCommand = onCommand,
            onDownloading = { percent ->
                if (percent < 1f) {
                    videoState.mute()
                } else {
                    videoState.seek(0)
                    videoState.unmute()
                }
                // do not hide
            },
            onInterruptions = {
                onConversationContinued()
            },
            onVoiceDetected = {
                videoState.mute()
                onConversationContinued()
            },
            onVoiceUnDetected = {
//                videoState.unmute()
            },
            onConversationEnds = {
//                videoState.reset()
//                onDismiss()
            },
            onThinking = onThinking,
            onError = { e ->
                when (videoState.source) {
                    VideoSources.Unavailable -> {
                        onDismiss() // todo records ?
                    }

                    VideoSources.Warning -> {
                        videoState.reset()
                    }

                    else -> {
                        Log.e(TAG, "Error in ai.", e)
                        videoState.unavailable()
                    }
                }
            },
            createKit = { context -> VoskKit(context) },
        )
        FrontCameraPreview(
            modifier = Modifier
                .padding(bottom = 48.dp)
                .fillMaxSize(),
            imageState = imageState,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                videoState.warning()
            }
    )
}
