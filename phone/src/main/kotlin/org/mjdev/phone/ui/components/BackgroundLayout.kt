/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.mjdev.phone.extensions.ComposeExt.rememberAssetImagePainter
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneAssets

@Previews
@Suppress("unused")
@Composable
fun BackgroundLayout(
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
    imageScale: ContentScale = ContentScale.Inside,
    content: @Composable () -> Unit = {},
) = PhoneTheme {
    val assetImageFile = phoneAssets.avatarImage
    val imagePainter: Painter = rememberAssetImagePainter(assetImageFile)
    MovieCard(
        modifier = modifier,
        borderSize = 0.dp,
        glowRadius = 0f,
        showImage = showImage,
        title = null,
        subtitle = null,
        lightColor = Color.White,
        lightColorRatio = 1f,
        image = imagePainter,
        useBackgroundFromPic = true,
        lightRatio = 1f,
        contentScale = imageScale,
        content = content,
    )
}
