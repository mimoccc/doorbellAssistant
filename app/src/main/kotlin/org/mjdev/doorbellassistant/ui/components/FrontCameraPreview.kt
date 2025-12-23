package org.mjdev.doorbellassistant.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.doorbellassistant.extensions.ComposeExt.EmptyBitmap
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.ui.theme.Black
import org.mjdev.doorbellassistant.ui.theme.DarkMD5
import org.mjdev.doorbellassistant.ui.theme.DoorBellAssistantTheme

@SuppressLint("ConfigurationScreenWidthHeight")
@Previews
@Composable
fun FrontCameraPreview(
    modifier: Modifier = Modifier,
    imageState: MutableState<Bitmap?> = remember { mutableStateOf(null) },
    portraitWidthRatio: Float = 0.4f,
    portraitHeightRatio: Float = 0.3f,
    landscapeWidthRatio: Float = 0.25f,
    landscapeHeightRatio: Float = 1f,
) = DoorBellAssistantTheme {
    Box(
        modifier = modifier,
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
        val shape = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(size)
                .background(DarkMD5, shape)
                .clip(shape)
                .border(2.dp, DarkMD5, shape)
                .shadow(4.dp, shape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black, shape),
                bitmap = (imageState.value ?: EmptyBitmap).asImageBitmap(),
                contentDescription = "",
                contentScale = ContentScale.Crop
            )
        }
    }
}
