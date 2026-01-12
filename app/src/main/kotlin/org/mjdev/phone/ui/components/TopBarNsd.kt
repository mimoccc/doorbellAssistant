package org.mjdev.phone.ui.components

import android.content.Context
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.phone.extensions.CustomExtensions.currentSystemUser
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.extensions.CustomExtensions.currentWifiSSID
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phoneIcons
import org.mjdev.phone.ui.theme.base.phoneShapes

@Previews
@Composable
fun TopBarNsd(
    modifier: Modifier = Modifier,
    applicationContext: Context = LocalContext.current.applicationContext,
    onClick: () -> Unit = {},
    onClickSettings: () -> Unit = {},
) = PhoneTheme {
    val userName: String = applicationContext.currentSystemUser
    val currentWifiSsid = LocalContext.current.currentWifiSSID
    val currentWifiIp = LocalContext.current.currentWifiIP
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
        Icon(
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
                .clickable(onClick = onClick),
            contentDescription = "",
            painter = phoneIcons.userAccountIcon,
            tint = phoneColors.colorIconTint,
        )
        SettingsIcon(
            modifier = Modifier.padding(end = 8.dp)
                .size(32.dp)
                .background(
                    color = phoneColors.colorIconsBackground,
                    shape = phoneShapes.settingsControlButtonShape
                )
                .align(Alignment.CenterEnd),
            shape = phoneShapes.settingsControlButtonShape
        )
    }
}
