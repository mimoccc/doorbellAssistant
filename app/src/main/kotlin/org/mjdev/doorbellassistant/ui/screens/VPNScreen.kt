package org.mjdev.doorbellassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.ui.components.VPNControls
import org.mjdev.doorbellassistant.ui.theme.Controls
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Previews
@Composable
fun VPNScreen(
    vpnState: MutableState<Boolean> = mutableStateOf(true),
    checkAndStartVpn: () -> Unit={},
    stopVpnService: () -> Unit={},
) = PhoneTheme {
    val controlsBackgroundColor: Color = Controls
    val controlsShape: Shape = CircleShape
    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .displayCutoutPadding()
            .fillMaxSize()
            .background(phoneColors.background)
    ) {
        VPNControls(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(controlsShape)
                .background(controlsBackgroundColor, controlsShape),
            state = vpnState,
            onStart = {
                checkAndStartVpn()
            },
            onStop = {
                stopVpnService()
            }
        )
    }
}