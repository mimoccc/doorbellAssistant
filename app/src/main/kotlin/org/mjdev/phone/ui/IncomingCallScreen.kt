package org.mjdev.phone.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.ui.components.BackgroundLayout

// todo caller no calle details
@Previews
@Composable
fun IncomingCallScreen(
    modifier: Modifier = Modifier,
    caller: NsdDevice? = NsdDevice.EMPTY,
    callee: NsdDevice? = NsdDevice.EMPTY,
    buttonSize: Dp = 80.dp,
    callerSize: Dp = 180.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onAccept: () -> Unit = {},
    onDeny: () -> Unit = {}
) {
    Box(
        modifier.background(backgroundColor)
    ) {
        BackgroundLayout(
            modifier = modifier.alpha(0.5f)
        )
        Box(
            modifier = Modifier
                .padding(bottom = 80.dp)
                .wrapContentSize()
                .align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier
                        .size(callerSize)
                        .clip(CircleShape)
                        .border(4.dp, White.copy(alpha = 0.5f), CircleShape), // todo
                    contentDescription = "",
                    imageVector = Icons.Filled.AccountCircle
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = (caller ?: callee)?.serviceType?.name ?: "Unknown",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = (caller ?: callee)?.address ?: "-",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(64.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter),
            horizontalArrangement = if (caller == null) Arrangement.SpaceBetween else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (caller == null) {
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
