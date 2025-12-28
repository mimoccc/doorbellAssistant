package org.mjdev.doorbellassistant.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.ai.type.PublicPreviewAPI
import org.mjdev.doorbellassistant.extensions.ComposeExt.isDesignMode
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.manager.AIManager.Companion.TAG
import org.mjdev.doorbellassistant.manager.AIManager.Companion.rememberAiManager
import org.mjdev.doorbellassistant.ui.theme.DoorBellAssistantTheme
import org.mjdev.doorbellassistant.ui.theme.Red
import org.mjdev.doorbellassistant.ui.theme.White

@Suppress("unused")
@Previews
@SuppressLint("MissingPermission")
@OptIn(PublicPreviewAPI::class, ExperimentalPermissionsApi::class)
@Composable
fun AISpeechRecognizer(
    modifier: Modifier = Modifier,
    speechState: MutableState<Boolean> = mutableStateOf(isDesignMode),
    onConversationEnds: () -> Unit = {},
    onVoiceDetected: () -> Unit = {},
    onConversationResponded: (String) -> Unit = {},
    onInterruptions: () -> Unit = {},
    onCommand: (String) -> Boolean = { false },
    onError: (Throwable) -> Unit = { e -> Log.e(TAG, "Error in ai.", e) }
) = DoorBellAssistantTheme {
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val aiManager = rememberAiManager(
        onCommand = onCommand,
        onConversationEnds = onConversationEnds,
        onConversationResponded = onConversationResponded,
        onError = onError,
    )
    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                if (speechState.value) Red else White.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp),
            ),
        verticalAlignment = Alignment.Bottom
    ) {
        Icon(
            modifier = Modifier
                .padding(2.dp)
                .size(32.dp)
                .background(
                    White.copy(alpha = 0.5f),
                    CircleShape
                )
                .padding(2.dp),
            tint = White,
            imageVector = if (speechState.value) Icons.Filled.Mic
            else Icons.Filled.MicOff,
            contentDescription = ""
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            text = "...",
            color = White
        )
    }
    LaunchedEffect(speechState.value) {
        aiManager.onInterrupt = {
            onInterruptions()
        }
        aiManager.onVoiceDetected = {
            onVoiceDetected()
        }
        aiManager.onConversationResponded = { text ->
            onConversationResponded(text)
        }
        aiManager.onConversationEnds = {
            onConversationEnds()
        }
        aiManager.onCommand = { cmd ->
            onCommand(cmd)
        }
        when (speechState.value) {
            true -> {
                if (micPermissionState.status.isGranted.not()) {
                    micPermissionState.launchPermissionRequest()
                }
                aiManager.startConversation()
            }

            false -> aiManager.stopConversation()
        }
    }
    DisposableEffect(speechState.value) {
        onDispose {
            aiManager.stopConversation()
        }
    }
}
