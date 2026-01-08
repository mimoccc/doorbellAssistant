package org.mjdev.phone.ui

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.phone.extensions.CustomExtensions.applyIf
import org.mjdev.phone.extensions.CustomExtensions.currentWifiIP
import org.mjdev.phone.extensions.CustomExtensions.isPreview
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdTypes
import org.mjdev.phone.nsd.device.rememberNsdDeviceList

@Suppress("DEPRECATION")
@OptIn(ExperimentalCoroutinesApi::class)
@Previews
@Composable
fun NsdList(
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
    onCallClick: (NsdDevice?) -> Unit = {},
    types: List<NsdTypes> = NsdTypes.entries,
) {
    val context = LocalContext.current
    val currentIP = context.currentWifiIP
    val isLandscape = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE
    val devices = if (isPreview) (1..32).map { i ->
        NsdDevice(NsdServiceInfo().apply {
            serviceName = "sn-$i"
        })
    } else rememberNsdDeviceList(
        types = types,
        onError = onError,
        filter = { s ->
            val serviceIP = s.host?.hostAddress ?: ""
            currentIP.contentEquals(serviceIP).not()
        }
    )
    Column(
        modifier = modifier
            .padding(top = 12.dp, start = 8.dp, end = 8.dp)
    ) {
        Spacer(
            modifier = Modifier.height(4.dp)
        )
        TopBarNsd(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
        Text(
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp, start = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            color = Color.Black, // todo
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            text = "Devices:",
            fontSize = 20.sp
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .applyIf(isLandscape) {
                    displayCutoutPadding()
                }
                .weight(1f)
        ) {
            items(
                items = devices.distinctBy { device -> device.serviceName ?: device.hashCode() },
                key = { device -> device.serviceName ?: device.hashCode() }
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
