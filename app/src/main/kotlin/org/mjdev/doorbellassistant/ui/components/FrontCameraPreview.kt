/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.extensions.CustomAppExt.rememberDeviceCapture
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.service.CallNsdService.Companion.nsdDevice
import org.mjdev.phone.ui.components.MovieCard
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Suppress("ParamsComparedByRef")
@SuppressLint("ConfigurationScreenWidthHeight")
@Previews
@Composable
fun FrontCameraPreview(
    modifier: Modifier = Modifier,
    portraitWidthRatio: Float = 0.4f,
    portraitHeightRatio: Float = 0.3f,
    landscapeWidthRatio: Float = 0.3f,
    landscapeHeightRatio: Float = 1f,
    onClick: () -> Unit = {},
    backgroundColor: Color = Color.Transparent,
    device: NsdDevice? = null,
) = PhoneTheme {
    val context = LocalContext.current
    var captureDevice by remember(device) {
        mutableStateOf(device)
    }
    val imageState = rememberDeviceCapture(captureDevice)
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomEnd
    ) {
        val config = LocalConfiguration.current
        val size = remember(config) {
            when (config.orientation) {
                ORIENTATION_LANDSCAPE -> DpSize(
                    (config.screenWidthDp * landscapeWidthRatio).dp,
                    (config.screenHeightDp * landscapeHeightRatio).dp
                )

                else -> DpSize(
                    (config.screenWidthDp * portraitWidthRatio).dp,
                    (config.screenHeightDp * portraitHeightRatio).dp
                )
            }
        }
        MovieCard(
            modifier = Modifier
                .padding(16.dp)
                .size(size),
            glowColor = phoneColors.colorGlow,
            glowRadius = 4f,
            contentScale = ContentScale.Crop,
            useBackgroundFromPic = false,
            borderSize = 2.dp,
            clearImageBackground = false,
            useShadows = false,
            backgroundColor = Color.DarkGray,
            title = "", // todo
            subtitle = "", // todo
            image = imageState.value?.let { bmp -> BitmapPainter(bmp.asImageBitmap()) }
        )
    }
    LaunchedEffect(device) {
        if (device == null && device != NsdDevice.EMPTY) {
            context.nsdDevice { device ->
                captureDevice = device
            }
        }
    }
}
