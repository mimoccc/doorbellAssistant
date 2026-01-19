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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.mjdev.phone.extensions.CustomExt.isPreview

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
    fun rememberAssetImage(
        assetImageFile: String = "avatar/avatar_transparent.png",
        onError: (Throwable) -> ImageBitmap = { ImageBitmap(1, 1) },
    ): ImageBitmap {
        val context: Context = LocalContext.current
        return remember(assetImageFile) {
            runCatching {
                context.assets.open(assetImageFile).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream).asImageBitmap()
                }
            }.onFailure { e ->
                onError(e)
            }.getOrNull()
                ?: onError(RuntimeException("Error loading: $assetImageFile from assets."))
        }
    }

    @Composable
    fun rememberAssetImagePainter(
        assetImageFile: String = "avatar/avatar_transparent.png",
        assetImage: ImageBitmap = rememberAssetImage(
            assetImageFile = assetImageFile
        ),
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
