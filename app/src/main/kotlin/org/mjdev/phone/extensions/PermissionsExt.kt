package org.mjdev.phone.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Suppress("unused")
object PermissionsExt {

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun rememberPermissionsState(
        permissions: List<String> = getPermissions(),
        onPermissionsResult: ((Map<String, Boolean>) -> Unit)? = null,
        onAllPermissionsGranted: (suspend () -> Unit)? = null,
    ): PermissionsState = if (LocalInspectionMode.current) fakePermissionsState else {
        val permissionState = rememberMultiplePermissionsState(permissions) {
            onPermissionsResult?.invoke(it)
        }
        val allPermissionsGranted = permissionState.allPermissionsGranted
        LaunchedEffect(allPermissionsGranted) {
            if (allPermissionsGranted) {
                onAllPermissionsGranted?.invoke()
            }
        }
        remember(permissions) {
            object : PermissionsState {
                override val allPermissionsGranted: Boolean
                    get() = permissionState.allPermissionsGranted
                override val shouldShowRationale: Boolean
                    get() = permissionState.shouldShowRationale

                override fun launchPermissionRequest() {
                    permissionState.launchMultiplePermissionRequest()
                }
            }
        }
    }

    @SuppressLint("UseKtx")
    @Composable
    fun LaunchPermissions(
        onPermissionsResult: ((Map<String, Boolean>) -> Unit)? = null,
        onAllPermissionsGranted: (suspend () -> Unit)? = null,
    ) {
        val context = LocalContext.current
        val permissions = getPermissions()
        val lifecycleOwner = LocalLifecycleOwner.current
        val hasOverlayPermission = Manifest.permission.SYSTEM_ALERT_WINDOW in permissions
        val overlayGranted = remember { mutableStateOf(Settings.canDrawOverlays(context)) }
        val regularPermissions = remember(permissions) {
            permissions.filter { it != Manifest.permission.SYSTEM_ALERT_WINDOW }
        }
        val overlayLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            overlayGranted.value = Settings.canDrawOverlays(context)
        }
        val permissionsState = rememberPermissionsState(
            permissions = regularPermissions,
            onPermissionsResult = { results ->
                val allResults = if (hasOverlayPermission) {
                    results + (Manifest.permission.SYSTEM_ALERT_WINDOW to overlayGranted.value)
                } else {
                    results
                }
                onPermissionsResult?.invoke(allResults)
            },
            onAllPermissionsGranted = {
                if (!hasOverlayPermission || overlayGranted.value) {
                    onAllPermissionsGranted?.invoke()
                }
            },
        )
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && hasOverlayPermission) {
                    overlayGranted.value = Settings.canDrawOverlays(context)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        LaunchedEffect(
            permissionsState,
            permissionsState.allPermissionsGranted,
            permissionsState.shouldShowRationale,
            overlayGranted.value
        ) {
            if (hasOverlayPermission && !overlayGranted.value) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                overlayLauncher.launch(intent)
            }
            if (regularPermissions.isNotEmpty()) {
                permissionsState.launchPermissionRequest()
            } else if (!hasOverlayPermission) {
                onAllPermissionsGranted?.invoke()
            }
        }
    }

    @Composable
    private fun getPermissions(): List<String> {
        val context = LocalContext.current
        return remember(context) {
            runCatching {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.PackageInfoFlags.of(
                            PackageManager.GET_PERMISSIONS.toLong()
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.GET_PERMISSIONS
                    )
                }
                packageInfo.requestedPermissions?.toList()
            }.getOrNull() ?: emptyList()
        }
    }

    internal val fakePermissionsState = object : PermissionsState {
        override val allPermissionsGranted: Boolean = false
        override val shouldShowRationale: Boolean = false
        override fun launchPermissionRequest() = Unit
    }

    @Stable
    interface PermissionsState {
        val allPermissionsGranted: Boolean
        val shouldShowRationale: Boolean
        fun launchPermissionRequest()
    }

}
