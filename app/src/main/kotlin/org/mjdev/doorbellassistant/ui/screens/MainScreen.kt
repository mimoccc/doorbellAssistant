package org.mjdev.doorbellassistant.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import org.mjdev.doorbellassistant.enums.VideoSources
import org.mjdev.doorbellassistant.extensions.ComposeExt.VisibleState
import org.mjdev.doorbellassistant.helpers.MotionDetector
import org.mjdev.doorbellassistant.ui.components.CartoonPlayerState
import org.mjdev.doorbellassistant.ui.components.CartoonPlayerState.Companion.rememberCartoonState
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Previews
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    motionState: MutableState<Boolean> = mutableStateOf(isPreview),
    onStartClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onWelcomeVideoFinished: () -> Unit = {},
    onConversationContinued: () -> Unit = {},
) = PhoneTheme {
    Box(
        modifier = modifier
    ) {
        VisibleState(
            visible = motionState.value.not()
        ) {
            LauncherScreen(
                modifier = modifier,
                onStartClicked = {
                    onStartClick()
                }
            )
        }
        VisibleState(
            visible = motionState.value
        ) {
            val videoState: CartoonPlayerState = rememberCartoonState(
                initialSource = VideoSources.Welcome,
                initialVisible = motionState.value
            )
            MotionAlertScreen(
                imageState = MotionDetector.latestBitmap,
                videoState = videoState,
                onWelcomeVideoFinished = onWelcomeVideoFinished,
                onConversationContinued = onConversationContinued,
                onDismiss = onDismiss,
                onCommand = { cmd ->
                    // todo
                    true
                }
            )
        }
    }
}
