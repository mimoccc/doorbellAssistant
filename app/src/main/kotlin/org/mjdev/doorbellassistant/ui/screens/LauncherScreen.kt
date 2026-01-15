package org.mjdev.doorbellassistant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.ui.theme.Black
import org.mjdev.doorbellassistant.ui.theme.White
import org.mjdev.phone.ui.components.GlowButton
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.components.BackgroundLayout
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Previews
@Composable
fun LauncherScreen(
    modifier: Modifier = Modifier,
    onStartClicked: () -> Unit = {},
) = PhoneTheme {
    Box(
        modifier = modifier.background(phoneColors.colorBackground),
        contentAlignment = Alignment.Center
    ) {
        BackgroundLayout(
            Modifier
                .fillMaxSize()
                .alpha(0.8f)
        )
        GlowButton(
            modifier = Modifier.size(120.dp),
            onClick = onStartClicked,
        ) {
            Icon(
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxSize(),
                contentDescription = "",
                tint = if (isPreview) {
                    if (isSystemInDarkTheme()) White else Black
                } else White,
                imageVector = Icons.Filled.BackHand
            )
        }
    }
}
