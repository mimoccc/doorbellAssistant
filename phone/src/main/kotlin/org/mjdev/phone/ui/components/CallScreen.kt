/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.mjdev.phone.extensions.ContextExt.currentWifiIP
import org.mjdev.phone.helpers.AudioPlayer
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneAssets
import org.mjdev.phone.ui.theme.base.phoneColors

@Suppress("ParamsComparedByRef")
@Previews
@Composable
fun CallScreen(
    modifier: Modifier = Modifier,
    caller: NsdDevice = NsdDevice.EMPTY,
    callee: NsdDevice = NsdDevice.EMPTY,
    buttonSize: Dp = 48.dp,
    callerSize: Dp = 120.dp,
    onAccept: () -> Unit = {},
    onDeny: () -> Unit = {},
) = PhoneTheme {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ringtone = phoneAssets.ringtoneAssetFile
    var ringtoneJob by remember { mutableStateOf<Job?>(null) }
    val audioPlayer = remember {
        AudioPlayer(
            sampleRate = 48000,
            outputStream = AudioPlayer.AudioOutputStream.MEDIA
        )
    }
    val isCaller = caller.address != context.currentWifiIP
    Box(
        modifier.background(phoneColors.colorBackground)
    ) {
        BackgroundLayout(
            modifier = modifier.alpha(0.5f)
        )
        CallerInfo(
            modifier = Modifier.fillMaxSize(),
            caller = caller,
            callee = callee,
            imageSize = callerSize,
            shape = CircleShape // todo customizable
        )
        Row(
            modifier = Modifier
                .padding(bottom = 64.dp)
                .height(64.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isCaller) {
                IconButton(
                    modifier = Modifier.size(buttonSize),
                    onClick = onAccept,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Green) // todo
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(buttonSize - 4.dp),
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End call",
                        tint = White // todo
                    )
                }
                Spacer(modifier = Modifier.width(128.dp))
            }
            IconButton(
                modifier = Modifier.size(buttonSize),
                onClick = onDeny,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Red)
            ) {
                Icon(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(buttonSize - 4.dp),
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End call",
                    tint = White // todo
                )
            }
        }
    }
    LaunchedEffect(isCaller) {
        if (isCaller) {
            ringtoneJob = scope.launch {
                while (isActive) {
                    try {
                        audioPlayer.playOggFromAssets(
                            context = context,
                            assetPath = ringtone,
                            onPlayFinish = {
                            },
                            onPlayError = { e ->
                                e.printStackTrace()
                            }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        break
                    }
                }
            }
        }
    }
    DisposableEffect(isCaller) {
        onDispose {
            ringtoneJob?.cancel()
        }
    }
}
