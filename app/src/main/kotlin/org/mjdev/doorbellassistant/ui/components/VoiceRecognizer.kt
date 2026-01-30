/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.phone.extensions.CustomExt.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phoneIcons

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("ParamsComparedByRef")
@Previews
@Composable
fun VoiceRecognizer(
    modifier: Modifier = Modifier,
    isListening: Boolean = isPreview,
    isThinking: Boolean = isPreview
) = PhoneTheme {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isListening) phoneIcons.buttonMicOn
            else phoneIcons.buttonMicOff,
            contentDescription = "",
            tint = phoneColors.colorIconTint,
            modifier = Modifier.size(32.dp)
        )
        if (isThinking) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                color = phoneColors.colorIconTint,
                trackColor = phoneColors.colorIconsBackground,
            )
        }
    }
}
