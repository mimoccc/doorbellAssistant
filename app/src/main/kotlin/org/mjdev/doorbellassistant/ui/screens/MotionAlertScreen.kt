package org.mjdev.doorbellassistant.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import org.mjdev.doorbellassistant.enums.VideoSources
import org.mjdev.doorbellassistant.manager.AIManager.Companion.TAG
import org.mjdev.doorbellassistant.ui.components.AISpeechRecognizer
import org.mjdev.doorbellassistant.ui.components.CartoonPlayer
import org.mjdev.doorbellassistant.ui.components.FrontCameraPreview
import org.mjdev.phone.extensions.CustomExtensions.isLandscape
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.components.BrushedBox
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@OptIn(UnstableApi::class)
@Previews
@Composable
fun MotionAlertScreen(
    imageState: MutableState<Bitmap?> = mutableStateOf(null),
    speechState: MutableState<Boolean> = mutableStateOf(isPreview),
    visibleState: MutableState<Boolean> = mutableStateOf(isPreview),
    onWelcomeVideoFinished: () -> Unit = {},
    onConversationContinued: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onCommand: (String) -> Boolean = { false },
) = PhoneTheme {
    val videoState: MutableState<VideoSources?> = remember(visibleState.value) {
        mutableStateOf(VideoSources.Welcome)
    }
    AnimatedVisibility(
        visible = visibleState.value,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(phoneColors.colorBackground)
                .clickable {
                    videoState.value = VideoSources.Warning
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = visibleState.value,
                enter = fadeIn(animationSpec = tween(1000)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                CartoonPlayer(
                    modifier = Modifier.fillMaxSize(),
                    state = videoState,
                    onPaused = {
                        speechState.value = false
                        false // todo check
                    },
                    onResumed = {
                        speechState.value = false
                        false // todo check
                    },
                    onVideoFinish = { p ->
                        speechState.value = false
                        when (videoState.value) {
                            VideoSources.Unavailable -> {
                                onDismiss() // todo records ?
                            }

                            VideoSources.Warning -> {
                                speechState.value = false
                                videoState.value = VideoSources.Welcome
                            }

                            VideoSources.Welcome -> {
                                p.mute()
                                speechState.value = true
                                onWelcomeVideoFinished()
                            }

                            else -> {

                            }
                        }
                    }
                )
            }
            BrushedBox(
                modifier = Modifier.fillMaxSize(),
                innerColor = Color.White.copy(alpha=0.01f),
                outerColor = phoneColors.colorBackground,
                paddingLeft = if (isLandscape) -200.dp else -100.dp,
                paddingTop = if (isLandscape) -200.dp else 50.dp,
                paddingRight = if (isLandscape) 440.dp else 0.dp,
                paddingBottom = if (isLandscape) 50.dp else 130.dp,
            )
            AISpeechRecognizer(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                speechState = speechState,
                onConversationResponded = {
                    onConversationContinued()
                },
                onCommand = onCommand,
                onInterruptions = {
                    onConversationContinued()
                },
                onVoiceDetected = {
                    onConversationContinued()
                },
                onConversationEnds = {
                    onDismiss()
                },
                onError = { e ->
                    when (videoState.value) {
                        VideoSources.Unavailable -> {
                            onDismiss() // todo records ?
                        }

                        VideoSources.Warning -> {
                            videoState.value = VideoSources.Welcome
                        }

                        else -> {
                            Log.e(TAG, "Error in ai.", e)
                            speechState.value = false
                            videoState.value = VideoSources.Unavailable
                        }
                    }
                }
            )
            FrontCameraPreview(
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .fillMaxSize(),
                imageState = imageState,
                onClick = {
                    videoState.value = VideoSources.Warning
                }
            )
            // todo don't touch layout here, better then before
        }
    }
}
