package org.mjdev.phone.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import org.mjdev.phone.extensions.CustomExtensions.isPortrait
import org.mjdev.phone.extensions.CustomExtensions.neonStroke
import org.mjdev.phone.extensions.CustomExtensions.rememberAssetImage
import org.mjdev.phone.helpers.Previews
import kotlin.math.min

@Suppress("unused")
@Previews
@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap? = rememberAssetImage("avatar_transparent.png"),
    title: String = "title",
    subtitle: String = "subtitle",
    contentScale: ContentScale = if (isPortrait) ContentScale.FillHeight else ContentScale.Inside,
    roundCorners: Boolean = true,
    roundCornersSize: Dp? = null,
    useBackgroundFromPic: Boolean = true,
    backgroundColor: Color? = null,
    borderSize: Dp? = null,
    imageScale: Float = 1f,
    glowColor: Color = Color.White,
    glowRadius: Float = 8f,
    content: @Composable () -> Unit = {},
) = _root_ide_package_.org.mjdev.phone.ui.theme.base.PhoneTheme {
    BoxWithConstraints(modifier) {
        val size = min(constraints.maxWidth, constraints.maxHeight)
        val shape = if (roundCorners) {
            if (roundCornersSize == null) RoundedCornerShape((size / 20).dp)
            else RoundedCornerShape(roundCornersSize)
        } else RectangleShape
        val background = if (useBackgroundFromPic) rememberMajorColor(bitmap)
        else backgroundColor ?: Color.Transparent
        val border = BorderStroke(
            ((borderSize?.value ?: (size / 60f))).dp,
            background
        )
        Card(
            modifier = modifier.neonStroke(
                glowColor = glowColor,
                glowRadius = glowRadius,
                shape = shape
            ),
            shape = shape,
            border = border
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(background),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = title,
                        contentScale = contentScale,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(imageScale)
                    )
                }
                content()
//                BrushedBox(
//                    modifier = Modifier.fillMaxSize(),
//                    innerColor = Color.Black.copy(alpha=0.3f),
//                    outerColor = phoneColors.background,
//                    paddingLeft = if (isLandscape) -200.dp else -100.dp,
//                    paddingTop = if (isLandscape) -200.dp else -150.dp,
//                    paddingRight = if (isLandscape) 420.dp else 0.dp,
//                    paddingBottom = if (isLandscape) 50.dp else 250.dp,
//                )
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            val gradient = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.5f to Color.Transparent,
                                    0.75f to Color.Black.copy(alpha = 0.5f),
                                    1.0f to Color.Black.copy(alpha = 0.9f)
                                )
                            )
                            onDrawBehind {
                                drawRect(gradient)
                            }
                        }
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun rememberMajorColor(
    bitmap: ImageBitmap? = null,
    defaultColor: Color? = null
): Color = remember(bitmap) {
    val default = defaultColor ?: Color.Transparent
    runCatching {
        if (bitmap == null) default else Palette
            .from(bitmap.asAndroidBitmap())
            .generate()
            .getDominantColor(default.toArgb())
            .let { dominantInt ->
                Color(dominantInt)
            }
    }.getOrNull() ?: default
}
