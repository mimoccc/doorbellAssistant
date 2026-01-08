package org.mjdev.phone.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.stream.CallEndReason

@Previews
@Composable
fun CallScreen(
    modifier: Modifier = Modifier,
    callerDevice: NsdDevice? = null,
    calleeDevice: NsdDevice? = null,
    onEndCall: (CallEndReason) -> Unit = {}
) = Box(
    modifier = modifier
) {
    VideoCall(
        modifier = Modifier.fillMaxSize(),
        callerDevice = callerDevice,
        calleeDevice = calleeDevice,
        onEndCall = { reason ->
            onEndCall(reason)
        }
    )
}