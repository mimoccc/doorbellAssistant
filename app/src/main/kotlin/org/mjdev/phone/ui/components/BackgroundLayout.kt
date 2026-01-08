package org.mjdev.phone.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import org.mjdev.phone.extensions.CustomExtensions.rememberAssetImagePainter
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.BrushedBox

@Previews
@Suppress("unused")
@Composable
fun BackgroundLayout(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    assetImageFile : String =  "avatar_transparent.png",
    imagePainter : Painter= rememberAssetImagePainter(
        assetImageFile = assetImageFile
    ),
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentDescription:String = ""
) = Box(
    modifier = modifier
) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = imagePainter,
        contentDescription = contentDescription,
    )
    BrushedBox(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = backgroundColor
    )
}