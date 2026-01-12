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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Suppress("ParamsComparedByRef")
@Previews
@Composable
fun CallScreen(
    modifier: Modifier = Modifier,
    caller: NsdDevice? = NsdDevice.EMPTY,
    callee: NsdDevice? = NsdDevice.EMPTY,
    buttonSize: Dp = 48.dp,
    callerSize: Dp = 120.dp,
    onAccept: () -> Unit = {},
    onDeny: () -> Unit = {}
) = PhoneTheme {
    val context = LocalContext.current
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
            if (caller?.address != context.currentWifiIP) {
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
}
