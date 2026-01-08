package org.mjdev.doorbellassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.ui.theme.Red
import org.mjdev.doorbellassistant.ui.theme.White
import org.mjdev.phone.helpers.Previews

@Previews
@Composable
fun VPNControls(
    modifier: Modifier = Modifier,
    state: MutableState<Boolean> = mutableStateOf(false),
    onStart: () -> Unit = {},
    onStop: () -> Unit = {}
) = Row(
    modifier = modifier.padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.Center
) {
    IconButton(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape),
        onClick = {
            if (state.value) onStop() else onStart()
        }
    ) {
        Icon(
            modifier = Modifier
                .size(79.dp)
                .clip(CircleShape)
                .background(White, CircleShape),
            imageVector = if (state.value) Icons.Default.StopCircle
            else Icons.Default.PlayCircle,
            contentDescription = if (state.value) "Stop" else "Start",
            tint = Red
        )
    }
}