package org.mjdev.phone.activity.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent as overrideSetContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.mjdev.phone.extensions.CustomExtensions.enableEdgeToEdge
import org.mjdev.phone.ui.theme.base.PhoneTheme

open class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // todo bck color
        enableEdgeToEdge(
            Color.Transparent,
            Color.Transparent
        )
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent {

        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 100)
        }
    }

    fun setContent(
        parent: CompositionContext? = null,
        content: @Composable () -> Unit,
    ) = overrideSetContent(parent, {
        PhoneTheme {
            content()
        }
    })
}
