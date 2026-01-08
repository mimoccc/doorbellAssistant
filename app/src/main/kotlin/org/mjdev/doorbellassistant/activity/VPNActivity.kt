package org.mjdev.doorbellassistant.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.mjdev.doorbellassistant.ui.components.VPNControls
import org.mjdev.doorbellassistant.ui.theme.Controls
import org.mjdev.doorbellassistant.vpn.AdVpnService
import org.mjdev.phone.activity.base.UnlockedActivity
import org.mjdev.phone.helpers.Previews

@Suppress("unused")
class VPNActivity : UnlockedActivity() {
    companion object {
        private val TAG = VPNActivity::class.simpleName
    }

    private val vpnState: MutableState<Boolean> = mutableStateOf(false)

    private val vpnStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getIntExtra(
                AdVpnService.VPN_UPDATE_STATUS_EXTRA,
                AdVpnService.VPN_STATUS_STOPPED
            ) ?: AdVpnService.VPN_STATUS_STOPPED
            vpnState.value = status == AdVpnService.VPN_STATUS_RUNNING
        }
    }

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnState.value = getSharedPreferences("state", MODE_PRIVATE)
            .getBoolean("isActive", false)
        setContent {
            MainScreen(vpnState = vpnState)
        }
    }

    @SuppressLint("WrongConstant")
    override fun onResume() {
        super.onResume()
        ContextCompat.registerReceiver(
            this,
            vpnStatusReceiver,
            IntentFilter(AdVpnService.VPN_UPDATE_STATUS_INTENT),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(vpnStatusReceiver)
    }

    private fun checkAndStartVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() = startForegroundService(
        Intent(this, AdVpnService::class.java).apply {
            putExtra("COMMAND", 0)
        }
    )

    private fun stopVpnService() = startService(
        Intent(this, AdVpnService::class.java).apply {
            putExtra("COMMAND", 2)
        }
    )

    @Previews
    @Composable
    fun MainScreen(
        vpnState: MutableState<Boolean> = mutableStateOf(true)
    ) {
        val controlsBackgroundColor: Color = Controls
        val controlsShape: Shape = CircleShape
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .displayCutoutPadding()
                .fillMaxSize()
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
}
