package org.mjdev.doorbellassistant.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.doorbellassistant.ui.theme.Black
import org.mjdev.doorbellassistant.ui.theme.Border
import org.mjdev.doorbellassistant.ui.theme.White
import org.mjdev.phone.extensions.CustomExtensions.currentSystemUser
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.extensions.CustomExtensions.currentWifiSSID
import org.mjdev.phone.helpers.Previews

@Previews
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onClickSettings: () -> Unit = {},
) {
    val applicationContext: Context = LocalContext.current.applicationContext
    val userName: String = applicationContext.currentSystemUser
    val currentWifiSsid = LocalContext.current.currentWifiSSID
    val currentWifiIp = LocalContext.current.currentWifiIP
    Box(
        modifier = modifier.wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = Border,
                    shape = CircleShape
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 88.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                .align(Alignment.Center)
        ) {
            Text(
                color = Black,
                text = userName,
                fontSize = 13.sp
            )
            Text(
                color = Black,
                text = currentWifiSsid,
                fontSize = 11.sp
            )
            Text(
                color = Black,
                text = currentWifiIp,
                fontSize = 11.sp
            )
        }
        Icon(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Border,
                    shape = CircleShape
                )
                .border(
                    2.dp,
                    White.copy(alpha = 0.3f),
                    CircleShape
                )
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentDescription = "",
            imageVector = Icons.Filled.AccountCircle
        )
        Icon(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(32.dp)
                .background(
                    color = White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .align(Alignment.CenterEnd)
                .clip(CircleShape)
                .clickable(onClick = onClickSettings),
            contentDescription = "",
            imageVector = Icons.Filled.Settings
        )
    }
}