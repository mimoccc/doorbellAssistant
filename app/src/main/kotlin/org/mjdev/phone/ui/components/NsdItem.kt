package org.mjdev.phone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phoneIcons
import org.mjdev.phone.ui.theme.base.phoneShapes

@Suppress("DEPRECATION")
@Previews
@Composable
fun NsdItem(
    modifier: Modifier = Modifier,
    device: NsdDevice? = null,
    onCallClick: () -> Unit = {},
    onDeviceClick: () -> Unit = {},
    showCallButton: Boolean = isPreview,
) = PhoneTheme {
    Box(
        modifier = modifier.wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = phoneColors.labelsBackground,
                    shape = phoneShapes.labelsShape
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 80.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                .align(Alignment.Center)
        ) {
            Text(
                color = phoneColors.textColor,
                text = device?.label ?: "-",
                fontSize = 13.sp
            )
            Text(
                color = phoneColors.textColor,
                text = device?.address ?: "-",
                fontSize = 11.sp
            )
            Text(
                color = phoneColors.textColor,
                text = device?.serviceName ?: "-",
                fontSize = 11.sp
            )
        }
        Icon(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = phoneColors.iconsBackground,
                    shape = phoneShapes.headerIconShape
                )
                .border(
                    2.dp,
                    phoneColors.callerIconBorderColor,
                    phoneShapes.headerIconShape
                )
                .clip(phoneShapes.headerIconShape)
                .clickable(onClick = onDeviceClick)
                .padding(8.dp),
            contentDescription = "",
            tint = phoneColors.iconTint,
            imageVector = device?.imageVector ?: NsdTypes.UNSPECIFIED.imageVector,
        )
        if (showCallButton) Icon(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(40.dp)
                .background(
                    color = phoneColors.iconsBackground,
                    shape = phoneShapes.callControlButtonShape
                )
                .align(Alignment.CenterEnd)
                .clip(phoneShapes.callControlButtonShape)
                .padding(2.dp)
                .clickable(onClick = onCallClick),
            contentDescription = "",
            painter = phoneIcons.itemCallIcon,
            tint = phoneColors.iconTint,
        )
    }
}
