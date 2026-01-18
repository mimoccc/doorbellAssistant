package org.mjdev.phone.ui.components

import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.device.rememberNsdDeviceList
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneStrings

@Suppress("DEPRECATION")
@OptIn(ExperimentalCoroutinesApi::class)
@Previews
@Composable
fun NsdList(
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
    onCallClick: (NsdDevice?) -> Unit = {},
    types: List<NsdTypes> = NsdTypes.entries,
) = PhoneTheme {
    val context = LocalContext.current
    val currentIP = context.currentWifiIP
    val devices = if (isPreview) (1..32).map { i ->
        NsdDevice(NsdServiceInfo().apply {
            serviceName = "sn-$i"
        })
    } else rememberNsdDeviceList(
        types = types,
        filter = { s ->
            val serviceIP = s.host?.hostAddress ?: ""
            currentIP.contentEquals(serviceIP).not()
        }
    )
    Column(
        modifier = modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp)
    ) {
        TopBarNsd(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        )
        TextLabel(
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp, start = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            label = phoneStrings.labelListDevices,
            fontSize = 20.sp
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(
                items = devices.distinctBy { device -> device.serviceName },
                key = { device -> device.serviceName }
            ) { device ->
                NsdItem(
                    device = device,
                    onCallClick = { onCallClick(device) },
                    showCallButton = true,
                )
            }
        }
    }
}
