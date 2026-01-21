/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.doorbellassistant.enums.VideoSources
import org.mjdev.doorbellassistant.helpers.MotionDetector
import org.mjdev.doorbellassistant.service.TTSService.Companion.rememberTTSService
import org.mjdev.doorbellassistant.ui.components.CartoonPlayerState.Companion.rememberCartoonState
import org.mjdev.phone.extensions.ComposeExt.VisibleState
import org.mjdev.phone.extensions.CustomExt.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme

@OptIn(ExperimentalCoroutinesApi::class)
@Previews
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    motionState: MutableState<Boolean> = mutableStateOf(isPreview),
    onStartClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onWelcomeVideoFinished: () -> Unit = {},
    onConversationContinued: () -> Unit = {},
    onThinking: () -> Unit = {},
) = PhoneTheme {
//    val activityState = rememberActivityState()
    val ttsService by rememberTTSService()
    Box(
        modifier = modifier
    ) {
        VisibleState(
            visible = motionState.value.not() //|| activityState.isPaused
        ) {
            LauncherScreen(
                modifier = modifier,
                onStartClicked = {
                    onStartClick()
                }
            )
        }
        VisibleState(
            visible = motionState.value //&& activityState.isResumed
        ) {
            MotionAlertScreen(
                imageState = MotionDetector.latestBitmap,
                videoState = rememberCartoonState(
                    initialSource = VideoSources.Welcome,
                    initialVisible = motionState.value
                ),
                onWelcomeVideoFinished = onWelcomeVideoFinished,
                onConversationContinued = onConversationContinued,
                onDismiss = onDismiss,
                onThinking = onThinking,
//                onCommand = { cmd ->
//                    // todo
//                    true
//                }
            )
        }
    }
    LaunchedEffect(ttsService) {
        if (ttsService != null) {
            ttsService?.talk("Vítejte v aplikaci bytový asistent.")
        }
    }
}



