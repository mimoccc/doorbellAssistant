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

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.mjdev.phone.data.User
import org.mjdev.phone.extensions.ContextExt.currentSystemUserName
import org.mjdev.phone.extensions.ContextExt.currentWifiIP
import org.mjdev.phone.extensions.ContextExt.currentWifiSSID
import org.mjdev.phone.extensions.ContextExt.getDeviceUser
import org.mjdev.phone.extensions.CustomExt.isPreview
import java.io.File

object ComposeExt {
    val isLandscape: Boolean
        @Composable
        get() {
            val config = LocalConfiguration.current
            return config.orientation == ORIENTATION_LANDSCAPE
        }

    val isPortrait: Boolean
        @Composable
        get() {
            val config = LocalConfiguration.current
            return config.orientation == ORIENTATION_PORTRAIT
        }

    @Composable
    fun VisibleState(
        visible: Boolean = false,
        content: @Composable () -> Unit = {},
    ) {
        if (visible) {
            content()
        }
    }

    @Composable
    fun rememberImageBitmapFromUri(
        uri: Uri,
        key : Any? = uri
    ): State<Painter?> {
        val context = LocalContext.current
        return produceState(initialValue = null, key) {
            withContext(Dispatchers.IO) {
                runCatching {
                    value = when {
                        uri == Uri.EMPTY -> {
                            null
                        }

                        uri.scheme == "content" -> {
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                BitmapFactory.decodeStream(input)?.let {
                                    BitmapPainter(it.asImageBitmap())
                                }
                            }
                        }

                        uri.scheme == "file" -> {
                            uri.path?.let { path ->
                                val exists = File(path).let { f -> f.exists() && f.isFile && f.length() > 0 }
                                if (exists) {
                                    BitmapFactory.decodeFile(path)?.let {
                                        BitmapPainter(it.asImageBitmap())
                                    }
                                } else null
                            }
                        }

                        else -> {
                            throw (RuntimeException("Can not load image : $uri"))
                        }
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                }
            }
        }
    }

    @Composable
    fun currentWifiIP(): String = LocalContext.current.currentWifiIP

    @Composable
    fun currentWifiSSID(): String = LocalContext.current.currentWifiSSID

    @Composable
    fun currentSystemUserName(): String = LocalContext.current.currentSystemUserName

    val EmptyPainter : BitmapPainter = BitmapPainter(ImageBitmap(1, 1))

    @Suppress("ParamsComparedByRef")
    @Composable
    fun currentUser(
        key: Any? = null
    ): State<User?> = LocalContext.current.let { context ->
        produceState(initialValue = null, key) {
            value = context.getDeviceUser().first()?.copy(
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    @Suppress("DEPRECATION")
    @Composable
    fun SetBarsOnSheetDialogWindow(
        navigationBarColor: Color,
        lightNavIcons: Boolean,
    ) {
        val view = LocalView.current
        DisposableEffect(navigationBarColor, lightNavIcons) {
            val dialogWindow = (view.parent as? DialogWindowProvider)?.window
            if (dialogWindow != null) {
                dialogWindow.decorView.setBackgroundDrawable(0.toDrawable())
                val oldColor = dialogWindow.navigationBarColor
                val controller = WindowCompat.getInsetsController(dialogWindow, view)
                val oldIcons = controller.isAppearanceLightNavigationBars
                dialogWindow.navigationBarColor = navigationBarColor.toArgb()
                controller.isAppearanceLightNavigationBars = lightNavIcons
                onDispose {
                    dialogWindow.navigationBarColor = oldColor
                    controller.isAppearanceLightNavigationBars = oldIcons
                }
            } else {
                onDispose { }
            }
        }
    }

    @Composable
    fun rememberAssetImage(
        assetImageFile: String = "avatar/avatar_yellow.png",
        onError: (Throwable) -> ImageBitmap = { ImageBitmap(1, 1) },
    ): ImageBitmap {
        val context: Context = LocalContext.current
        return remember(assetImageFile) {
            runCatching {
                context.assets.open(assetImageFile).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream).asImageBitmap()
                }
            }.onFailure { e ->
                e.printStackTrace()
                onError(e)
            }.getOrNull()
                ?: onError(RuntimeException("Error loading: $assetImageFile from assets."))
        }
    }

    @Composable
    fun rememberAssetImagePainter(
        assetImageFile: String = "avatar/avatar_yellow.png",
        assetImage: ImageBitmap = rememberAssetImage(assetImageFile),
    ): Painter = remember(assetImageFile, assetImage) {
        BitmapPainter(assetImage)
    }

    @Composable
    fun rememberLifeCycleOwnerState(): State<LifecycleOwner?> {
        val lifecycleOwner = if (isPreview) null else LocalLifecycleOwner.current
        return rememberUpdatedState(lifecycleOwner)
    }

    operator fun PaddingValues.plus(other: PaddingValues) = object : PaddingValues {
        override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
            this@plus.calculateLeftPadding(layoutDirection) +
                    other.calculateLeftPadding(layoutDirection)

        override fun calculateTopPadding() =
            this@plus.calculateTopPadding() + other.calculateTopPadding()

        override fun calculateRightPadding(layoutDirection: LayoutDirection) =
            this@plus.calculateRightPadding(layoutDirection) +
                    other.calculateRightPadding(layoutDirection)

        override fun calculateBottomPadding() =
            this@plus.calculateBottomPadding() + other.calculateBottomPadding()
    }
}
