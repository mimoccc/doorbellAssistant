package org.mjdev.doorbellassistant.ui.components

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.doorbellassistant.extensions.ComposeExt.applyIf
import org.mjdev.doorbellassistant.extensions.ComposeExt.currentWifiIP
import org.mjdev.doorbellassistant.extensions.ComposeExt.isDesignMode
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.nsd.device.NsdDevice
import org.mjdev.doorbellassistant.nsd.device.NsdTypes
import org.mjdev.doorbellassistant.nsd.device.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.nsd.device.NsdTypes.DOOR_BELL_CLIENT
import org.mjdev.doorbellassistant.nsd.device.rememberNsdDeviceList
import org.mjdev.doorbellassistant.ui.theme.Black

@Suppress("DEPRECATION")
@OptIn(ExperimentalCoroutinesApi::class)
@Previews
@Composable
fun NsdList(
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
    onCallClick: (NsdDevice?) -> Unit = {},
    types: List<NsdTypes> = listOf(DOOR_BELL_ASSISTANT, DOOR_BELL_CLIENT),
) {
    val context = LocalContext.current
    val currentIP = context.currentWifiIP
    val isLandscape = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE
    val devices = if (isDesignMode) (1..32).map { i ->
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
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
        Text(
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp, start = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            color = Black,
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
