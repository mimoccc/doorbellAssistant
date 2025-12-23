package org.mjdev.doorbellassistant.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import org.mjdev.doorbellassistant.extensions.ComposeExt.isDesignMode
import org.mjdev.doorbellassistant.helpers.MotionDetector
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.ui.theme.DoorBellAssistantTheme

@Previews
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    motionState: MutableState<Boolean> = mutableStateOf(isDesignMode),
    speechState: MutableState<Boolean> = mutableStateOf(isDesignMode),
    onStartClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onWelcomeVideoFinished: () -> Unit = {},
    onConversationContinued: () -> Unit = {},
) = DoorBellAssistantTheme {
    Box {
        LauncherScreen(
            modifier = modifier,
            visibleState = motionState,
            onStartClicked = {
                onStartClick()
            },
            onDismiss = {
                onStartClick()
            }
        )
        MotionAlertScreen(
            visibleState = motionState,
            imageState = MotionDetector.latestBitmap,
            speechState = speechState,
            onWelcomeVideoFinished = onWelcomeVideoFinished,
            onConversationContinued = onConversationContinued,
            onDismiss = {
                speechState.value = false
                onDismiss()
            },
        )
    }
}
