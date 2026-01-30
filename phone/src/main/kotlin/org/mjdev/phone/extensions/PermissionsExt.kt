/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Suppress("unused")
object PermissionsExt {
    interface SpecialPermissionHandler {
        val permission: String
        fun isGranted(context: Context): Boolean
        fun createIntent(context: Context): Intent
    }

    object OverlayPermissionHandler : SpecialPermissionHandler {
        override val permission: String = Manifest.permission.SYSTEM_ALERT_WINDOW
        override fun isGranted(context: Context): Boolean = Settings.canDrawOverlays(context)
        override fun createIntent(context: Context): Intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${context.packageName}".toUri()
        )
    }

    object ManageExternalStorageHandler : SpecialPermissionHandler {
        override val permission: String = "android.permission.MANAGE_EXTERNAL_STORAGE"
        override fun isGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                android.os.Environment.isExternalStorageManager()
            else true
        }
        @RequiresApi(Build.VERSION_CODES.R)
        override fun createIntent(context: Context): Intent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            "package:${context.packageName}".toUri()
        )
    }

    object AccessibilityServiceHandler : SpecialPermissionHandler {
        override val permission: String = "android.permission.BIND_ACCESSIBILITY_SERVICE"
        override fun isGranted(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            return enabledServices.contains(context.packageName)
        }
        override fun createIntent(context: Context): Intent = Intent(
            Settings.ACTION_ACCESSIBILITY_SETTINGS
        )
    }

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
        specialHandlers: List<SpecialPermissionHandler> = listOf(
            OverlayPermissionHandler,
            ManageExternalStorageHandler
        ),
        onPermissionsResult: ((Map<String, Boolean>) -> Unit)? = null,
        onAllPermissionsGranted: (suspend () -> Unit)? = null,
    ) {
        val context = LocalContext.current
        val permissions = getPermissions()
        val lifecycleOwner = LocalLifecycleOwner.current
        val permissionRequested = remember { mutableStateOf(false) }
        val specialPermissions = permissions.filter { perm ->
            specialHandlers.any { it.permission == perm }
        }
        val regularPermissions = permissions.filter { perm ->
            specialHandlers.none { it.permission == perm }
        }
        val specialPermissionStates = specialHandlers.associateWith { handler ->
            remember { mutableStateOf(handler.isGranted(context)) }
        }
        val specialPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            specialPermissionStates.forEach { (handler, state) ->
                state.value = handler.isGranted(context)
            }
        }
        val permissionsState = rememberPermissionsState(
            permissions = regularPermissions,
            onPermissionsResult = { results ->
                val specialResults = specialPermissionStates.mapKeys { it.key.permission }
                    .mapValues { it.value.value }
                val allResults = results + specialResults
                onPermissionsResult?.invoke(allResults)
            },
            onAllPermissionsGranted = {
                val allSpecialGranted = specialPermissionStates.all { it.value.value }
                if (allSpecialGranted) {
                    onAllPermissionsGranted?.invoke()
                }
            },
        )
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    specialPermissionStates.forEach { (handler, state) ->
                        state.value = handler.isGranted(context)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        LaunchedEffect(Unit) {
            if (!permissionRequested.value) {
                permissionRequested.value = true
                val ungrantedSpecialHandler = specialPermissionStates.entries
                    .firstOrNull { !it.value.value }?.key
                if (ungrantedSpecialHandler != null) {
                    specialPermissionLauncher.launch(ungrantedSpecialHandler.createIntent(context))
                }
                if (regularPermissions.isNotEmpty() && !permissionsState.allPermissionsGranted) {
                    permissionsState.launchPermissionRequest()
                } else if (ungrantedSpecialHandler == null) {
                    onAllPermissionsGranted?.invoke()
                }
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
