package org.mjdev.phone.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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

/**
 * Extensible permission handler for Android applications.
 *
 * Features:
 * - Handles regular runtime permissions automatically from AndroidManifest.xml
 * - Supports special permissions via SpecialPermissionHandler interface
 * - Built-in handlers for:
 *   - Overlay permission (SYSTEM_ALERT_WINDOW)
 *   - Manage external storage (Android 11+)
 *   - Accessibility service
 *
 * Usage examples:
 *
 * 1. Default (overlay permission only):
 * ```kotlin
 * LaunchPermissions(
 *     onAllPermissionsGranted = { /* All permissions granted */ }
 * )
 * ```
 * 
 * 2. With external storage:
 * ```kotlin
 * LaunchPermissions(
 *     specialHandlers = listOf(
 *         OverlayPermissionHandler,
 *         ManageExternalStorageHandler
 *     ),
 *     onAllPermissionsGranted = { /* All permissions granted */ }
 * )
 * ```
 * 
 * 3. With accessibility service:
 * ```kotlin
 * LaunchPermissions(
 *     specialHandlers = listOf(
 *         OverlayPermissionHandler,
 *         AccessibilityServiceHandler
 *     ),
 *     onAllPermissionsGranted = { /* All permissions granted */ }
 * )
 * ```
 * 
 * 4. Custom permission handler:
 * ```kotlin
 * object MyCustomHandler : SpecialPermissionHandler {
 *     override val permission = "my.custom.PERMISSION"
 *     override fun isGranted(context: Context) = /* check logic */
 *     override fun createIntent(context: Context) = /* settings intent */
 * }
 * 
 * LaunchPermissions(
 *     specialHandlers = listOf(MyCustomHandler),
 *     onAllPermissionsGranted = { /* All permissions granted */ }
 * )
 * ```
 */
@Suppress("unused")
object PermissionsExt {

    /**
     * Interface for handling special permissions that require custom logic
     * (e.g., overlay permission, accessibility service, manage external storage)
     */
    interface SpecialPermissionHandler {
        val permission: String
        fun isGranted(context: Context): Boolean
        fun createIntent(context: Context): Intent
    }

    /**
     * Built-in handler for SYSTEM_ALERT_WINDOW (overlay) permission
     */
    object OverlayPermissionHandler : SpecialPermissionHandler {
        override val permission: String = Manifest.permission.SYSTEM_ALERT_WINDOW
        override fun isGranted(context: Context): Boolean = Settings.canDrawOverlays(context)
        override fun createIntent(context: Context): Intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }

    /**
     * Handler for MANAGE_EXTERNAL_STORAGE permission (Android 11+)
     */
    object ManageExternalStorageHandler : SpecialPermissionHandler {
        override val permission: String = "android.permission.MANAGE_EXTERNAL_STORAGE"
        override fun isGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.os.Environment.isExternalStorageManager()
            } else {
                true // Not needed on older versions
            }
        }
        override fun createIntent(context: Context): Intent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }

    /**
     * Handler for BIND_ACCESSIBILITY_SERVICE permission
     */
    object AccessibilityServiceHandler : SpecialPermissionHandler {
        override val permission: String = "android.permission.BIND_ACCESSIBILITY_SERVICE"
        override fun isGranted(context: Context): Boolean {
            // Check if accessibility service is enabled
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
        specialHandlers: List<SpecialPermissionHandler> = listOf(OverlayPermissionHandler),
        onPermissionsResult: ((Map<String, Boolean>) -> Unit)? = null,
        onAllPermissionsGranted: (suspend () -> Unit)? = null,
    ) {
        val context = LocalContext.current
        val permissions = getPermissions()
        val lifecycleOwner = LocalLifecycleOwner.current
        val permissionRequested = remember { mutableStateOf(false) }
        // Separate regular and special permissions
        val specialPermissions = permissions.filter { perm ->
            specialHandlers.any { it.permission == perm }
        }
        val regularPermissions = permissions.filter { perm ->
            specialHandlers.none { it.permission == perm }
        }
        // Track special permission states
        val specialPermissionStates = specialHandlers.associateWith { handler ->
            remember { mutableStateOf(handler.isGranted(context)) }
        }
        // Activity result launcher for special permissions
        val specialPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            // Update all special permission states after returning from settings
            specialPermissionStates.forEach { (handler, state) ->
                state.value = handler.isGranted(context)
            }
        }
        // Regular permissions state
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
        // Monitor lifecycle for special permissions
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
        // Launch permission requests once
        LaunchedEffect(Unit) {
            if (!permissionRequested.value) {
                permissionRequested.value = true
                // Request special permissions first
                val ungrantedSpecialHandler = specialPermissionStates.entries
                    .firstOrNull { !it.value.value }?.key
                if (ungrantedSpecialHandler != null) {
                    specialPermissionLauncher.launch(ungrantedSpecialHandler.createIntent(context))
                }
                // Then request regular permissions
                if (regularPermissions.isNotEmpty() && !permissionsState.allPermissionsGranted) {
                    permissionsState.launchPermissionRequest()
                } else if (ungrantedSpecialHandler == null) {
                    // All permissions granted
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
