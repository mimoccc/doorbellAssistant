package org.mjdev.phone.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.mjdev.phone.extensions.CustomExtensions.isLandscape
import org.mjdev.phone.extensions.CustomExtensions.rememberAssetImagePainter
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Previews
@Suppress("unused")
@Composable
fun BackgroundLayout(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    assetImageFile: String = "avatar_transparent.png",
    imagePainter: Painter = rememberAssetImagePainter(
        assetImageFile = assetImageFile
    ),
    contentDescription: String = "",
) = PhoneTheme {
    Box(
        modifier = modifier.background(
            phoneColors.background
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = imagePainter,
            contentDescription = contentDescription,
        )
        BrushedBox(
            modifier = Modifier.fillMaxSize(),
            innerColor = Color.Black.copy(alpha=0.4f),
            outerColor = phoneColors.background,
            paddingLeft = if (isLandscape) -200.dp else -100.dp,
            paddingTop = if (isLandscape) -200.dp else 50.dp,
            paddingRight = if (isLandscape) 440.dp else 0.dp,
            paddingBottom = if (isLandscape) 50.dp else 130.dp,
        )
    }
}
