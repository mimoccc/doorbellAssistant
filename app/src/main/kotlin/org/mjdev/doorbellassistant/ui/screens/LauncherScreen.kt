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

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.service.TTSService.Companion.rememberTTSService
import org.mjdev.doorbellassistant.ui.theme.Black
import org.mjdev.doorbellassistant.ui.theme.White
import org.mjdev.phone.extensions.CustomExt.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.components.BackgroundLayout
import org.mjdev.phone.ui.components.GlowButton
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Previews
@Composable
fun LauncherScreen(
    modifier: Modifier = Modifier,
    onStartClicked: () -> Unit = {},
) = PhoneTheme {
    val ttsService by rememberTTSService()
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
    LaunchedEffect(ttsService) {
        if (ttsService != null) {
            ttsService?.talk("Hello from door bell assistant.")
        }
    }
}
