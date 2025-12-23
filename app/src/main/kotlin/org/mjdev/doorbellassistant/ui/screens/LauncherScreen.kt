package org.mjdev.doorbellassistant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.extensions.ComposeExt.isDesignMode
import org.mjdev.doorbellassistant.extensions.ComposeExt.rememberAssetImage
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.ui.components.BrushedBox
import org.mjdev.doorbellassistant.ui.theme.DoorBellAssistantTheme
import org.mjdev.doorbellassistant.ui.theme.White

@Previews
@Composable
fun LauncherScreen(
    modifier: Modifier = Modifier,
    visibleState: MutableState<Boolean> = mutableStateOf(!isDesignMode),
    onStartClicked: () -> Unit = {},
    onDismiss: () -> Unit = {},
) = DoorBellAssistantTheme {
    AnimatedVisibility(
        modifier = modifier.clickable { onDismiss() },
        visible = visibleState.value.not(),
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f),
                bitmap = rememberAssetImage("avatar_transparent.png"),
                contentDescription = "",
            )
            BrushedBox(
                modifier = Modifier.fillMaxSize(),
            )
            Button(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .shadow(8.dp),
                onClick = onStartClicked
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 4.dp),
                    contentDescription = "",
                    tint = White,
                    imageVector = Icons.Filled.BackHand
                )
            }
        }
    }
}
