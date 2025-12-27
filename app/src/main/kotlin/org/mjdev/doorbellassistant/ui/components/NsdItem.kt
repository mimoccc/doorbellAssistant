package org.mjdev.doorbellassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.doorbellassistant.extensions.ComposeExt.isDesignMode
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.helpers.nsd.device.NsdTypes.UNSPECIFIED
import org.mjdev.doorbellassistant.ui.theme.Black
import org.mjdev.doorbellassistant.ui.theme.Item
import org.mjdev.doorbellassistant.ui.theme.White

@Suppress("DEPRECATION")
@Previews
@Composable
fun NsdItem(
    modifier: Modifier = Modifier,
    device: NsdDevice? = null,
    onCallClick: () -> Unit = {},
    onDeviceClick: () -> Unit = {},
    showCallButton: Boolean = isDesignMode
) {
    Box(
        modifier = modifier.wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = Item,
                    shape = CircleShape
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 80.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                .align(Alignment.Center)
        ) {
            Text(
                color = Black,
                text = device?.label ?: "-",
                fontSize = 13.sp
            )
            Text(
                color = Black,
                text = device?.address ?: "-",
                fontSize = 11.sp
            )
            Text(
                color = Black,
                text = device?.serviceName ?: "-",
                fontSize = 11.sp
            )
        }
        Icon(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = Item,
                    shape = CircleShape
                )
                .border(
                    2.dp,
                    White.copy(alpha = 0.6f),
                    CircleShape
                )
                .clip(CircleShape)
                .clickable(onClick = onDeviceClick)
                .padding(8.dp),
            contentDescription = "",
            imageVector = device?.imageVector ?: UNSPECIFIED.imageVector
        )
        if (showCallButton) Icon(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(40.dp)
                .background(
                    color = White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .align(Alignment.CenterEnd)
                .clip(CircleShape)
                .padding(2.dp)
                .clickable(onClick = onCallClick),
            contentDescription = "",
            imageVector = Icons.Filled.Call
        )
    }
}

