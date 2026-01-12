package org.mjdev.doorbellassistant.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.extensions.ComposeExt.EmptyBitmap
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.components.MovieCard
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@SuppressLint("ConfigurationScreenWidthHeight")
@Previews
@Composable
fun FrontCameraPreview(
    modifier: Modifier = Modifier,
    imageState: MutableState<Bitmap?> = remember { mutableStateOf(null) },
    portraitWidthRatio: Float = 0.4f,
    portraitHeightRatio: Float = 0.3f,
    landscapeWidthRatio: Float = 0.3f,
    landscapeHeightRatio: Float = 1f,
    onClick: () -> Unit = {},
    backgroundColor: Color = Color.Transparent,
) = PhoneTheme {
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
            bitmap = (imageState.value ?: EmptyBitmap).asImageBitmap()
        )
    }
}
