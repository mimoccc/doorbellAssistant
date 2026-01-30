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

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import org.mjdev.phone.data.User
import org.mjdev.phone.extensions.ComposeExt.currentWifiIP
import org.mjdev.phone.extensions.ComposeExt.currentWifiSSID
import org.mjdev.phone.extensions.ComposeExt.rememberImageBitmapFromUri
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phoneIcons
import org.mjdev.phone.ui.theme.base.phoneShapes
import org.mjdev.phone.vector.ImageVectorProvider.createVectorPainter

@Suppress("ParamsComparedByRef")
@Previews
@Composable
fun TopBarNsd(
    modifier: Modifier = Modifier,
    user: User? = null,
    onUserPicClick: () -> Unit = {},
    onClickSettings: () -> Unit = {},
) = PhoneTheme {
    val defaultPicPainter = createVectorPainter(phoneIcons.userAccountIcon)
    val userName: String by remember(user?.lastUpdated) {
        derivedStateOf {
            user?.name ?: ""
        }
    }
    val userPicUri: Uri by remember(user?.lastUpdated) {
        derivedStateOf {
            user?.photoUri?.toUri() ?: Uri.EMPTY
        }
    }
    val userBitmap by rememberImageBitmapFromUri(
        userPicUri,
        user?.lastUpdated
    )
    val userPicPainter: Painter by remember(user?.lastUpdated) {
        derivedStateOf {
            userBitmap ?: defaultPicPainter
        }
    }
    val currentWifiSsid = currentWifiSSID()
    val currentWifiIp = currentWifiIP()
    Box(
        modifier = modifier.wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = phoneColors.colorLabelsBackground,
                    shape = phoneShapes.deviceLogoShape
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 88.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                .align(Alignment.Center)
        ) {
            Text(
                color = phoneColors.colorText,
                text = userName,
                fontSize = 13.sp
            )
            Text(
                color = phoneColors.colorText,
                text = currentWifiSsid,
                fontSize = 11.sp
            )
            Text(
                color = phoneColors.colorText,
                text = currentWifiIp,
                fontSize = 11.sp
            )
        }
        ChromedImage(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = phoneColors.colorIconsBackground,
                    shape = phoneShapes.headerLogoShape
                )
                .border(
                    2.dp,
                    phoneColors.colorCallerIconBorder,
                    phoneShapes.headerLogoShape
                )
                .clip(phoneShapes.headerLogoShape)
                .clickable(onClick = onUserPicClick),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            painter = userPicPainter,
            backgroundColor = phoneColors.colorBackground,
            colorFilter = if (userBitmap == null) ColorFilter.tint(phoneColors.colorIconTint)
            else null
        )
        SettingsIcon(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(32.dp)
                .background(
                    color = phoneColors.colorIconsBackground,
                    shape = phoneShapes.settingsControlButtonShape
                )
                .align(Alignment.CenterEnd),
            shape = phoneShapes.settingsControlButtonShape,
            onClickSettings = onClickSettings
        )
    }
}
