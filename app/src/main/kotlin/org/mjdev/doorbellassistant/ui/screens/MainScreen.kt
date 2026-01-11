package org.mjdev.doorbellassistant.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import org.mjdev.doorbellassistant.helpers.MotionDetector
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Previews
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    motionState: MutableState<Boolean> = mutableStateOf(isPreview),
    speechState: MutableState<Boolean> = mutableStateOf(isPreview),
    onStartClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onWelcomeVideoFinished: () -> Unit = {},
    onConversationContinued: () -> Unit = {},
) = PhoneTheme {
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
