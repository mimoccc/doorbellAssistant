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

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.phone.extensions.ContextExt.currentWifiIP
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Previews
@Composable
fun CallerInfo(
    modifier: Modifier = Modifier,
    caller: NsdDevice? = null,
    callee: NsdDevice? = null,
    imageSize: Dp = 128.dp,
    shape: Shape = CircleShape
) = PhoneTheme {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val who: State<NsdDevice> = remember(caller, callee) {
            derivedStateOf {
                if (caller?.address == context.currentWifiIP) {
                    callee ?: NsdDevice.EMPTY
                } else {
                    caller ?: NsdDevice.EMPTY
                }
            }
        }
        Image(
            modifier = Modifier
                .size(imageSize)
                .clip(shape)
                .border(
                    4.dp,
                    phoneColors.colorCallScreenText,
                    shape
                ),
            contentDescription = "",
            imageVector = Icons.Filled.AccountCircle, // todo
            colorFilter = ColorFilter.tint(phoneColors.colorCallScreenText)
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = who.value.serviceType.label,
            color = phoneColors.colorCallScreenText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = who.value.address,
            color = phoneColors.colorCallScreenText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
