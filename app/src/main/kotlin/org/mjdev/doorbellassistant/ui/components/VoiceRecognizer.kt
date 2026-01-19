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

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel
import org.mjdev.doorbellassistant.agent.stt.transcribers.vosk.VoskModelType
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("ParamsComparedByRef")
@Previews
@Composable
fun VoiceRecognizer(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    modelType: ITKitModel = VoskModelType.CS_SMALL,
    voiceDetectionSensitivity: Float = 0.2f,
    stopListeningWhenNoVoiceAtLeast: Float = 2.0f,
    maxRecordingDurationMs: Long = 20000L,
    minRecordingDurationMs: Long = 2000L,
    autoStart: Boolean = false,
    iconTint: Color = Color.White,
) = PhoneTheme {
    var isListening by remember { mutableStateOf(false)}
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = "",
            tint = iconTint,
            modifier = Modifier.size(32.dp)
        )
    }
}
