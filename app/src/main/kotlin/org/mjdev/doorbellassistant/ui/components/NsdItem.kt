package org.mjdev.doorbellassistant.ui.components

import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes
import org.mjdev.doorbellassistant.helpers.nsd.NsdTypes.UNSPECIFIED
import org.mjdev.doorbellassistant.ui.theme.Black
import org.mjdev.doorbellassistant.ui.theme.White

@Suppress("DEPRECATION")
@Previews
@Composable
fun NsdItem(
    serviceType: NsdTypes = UNSPECIFIED,
    service: NsdServiceInfo? = null,
    onClick: () -> Unit = {},
) = Box(
    modifier = Modifier
        .padding(bottom = 8.dp)
        .fillMaxWidth()
        .wrapContentHeight()
        .background(
            color = White.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp)
        ).clickable(onClick = onClick),
) {
    Row(
        modifier = Modifier
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .background(
                    color = White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .padding(8.dp),
            contentDescription = "",
            imageVector = serviceType.imageVector
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                color = Black,
                text = "${service?.host?.hostAddress ?: "-"}:${service?.port ?: "-"}",
                fontSize = 18.sp
            )
            Text(
                color = Black,
                text = service?.serviceName ?: serviceType.name,
                fontSize = 14.sp
            )
        }
    }
}
