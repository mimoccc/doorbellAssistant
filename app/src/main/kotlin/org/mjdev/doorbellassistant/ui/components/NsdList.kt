package org.mjdev.doorbellassistant.ui.components

import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_ASSISTANT
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.DOOR_BELL_CLIENT
import org.mjdev.doorbellassistant.helpers.nsd.rememberNsdServicesList
import org.mjdev.doorbellassistant.ui.theme.Black

@OptIn(ExperimentalCoroutinesApi::class)
@Previews
@Composable
fun NsdList(
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
    onClick: (NsdServiceInfo) -> Unit = {},
    types: List<NsdTypes> = listOf(DOOR_BELL_ASSISTANT, DOOR_BELL_CLIENT),
) = Column(
    modifier = modifier.padding(16.dp)
) {
    val services = rememberNsdServicesList(
        types = types,
        onError = onError
    )
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = Black,
        text = "Devices:",
        fontSize = 14.sp
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
    ) {
        items(
            items = services,
            key = { service -> service.serviceName }
        ) { service ->
            NsdItem(
                serviceType = NsdTypes(service.serviceType),
                service = service,
                onClick = { onClick(service) }
            )
        }
    }
}
