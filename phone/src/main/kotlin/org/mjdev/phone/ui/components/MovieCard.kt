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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import org.mjdev.phone.extensions.ColorExt.darker
import org.mjdev.phone.extensions.ColorExt.lighter
import org.mjdev.phone.extensions.ComposeExt.EmptyPainter
import org.mjdev.phone.extensions.ComposeExt.rememberAssetImagePainter
import org.mjdev.phone.extensions.CustomExt.isPreview
import org.mjdev.phone.extensions.CustomExt.toImageBitmap
import org.mjdev.phone.extensions.ModifierExt.neonStroke
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import kotlin.math.min

// todo image preview error
@Suppress("unused")
@Previews
@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    image: Painter? = rememberAssetImagePainter("avatar/avatar_yellow.png"),
    title: String? = if (isPreview) "title" else null,
    subtitle: String? = if (isPreview) "subtitle" else null,
    contentScale: ContentScale = ContentScale.Inside,
    useBackgroundFromPic: Boolean = true,
    roundCorners: Boolean = true,
    clearImageBackground: Boolean = true,
    useShadows: Boolean = true,
    roundCornersSize: Dp? = null,
    backgroundColor: Color? = null,
    borderSize: Dp? = null,
    imageScale: Float = 1f,
    glowColor: Color = if (isSystemInDarkTheme()) Color.White else Color.Black,
    glowRadius: Float = 8f,
    lightColor: Color? = null,
    lightRatio: Float = 0.8f,
    lightColorRatio: Float = 0.4f,
    shadowingColor: Color? = null,
    shadowRatio: Float = 0.6f,
    shadowColorRatio: Float = 0.2f,
    showImage: Boolean = true,
    content: @Composable (() -> Unit)? = null,
) = PhoneTheme {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val size = min(constraints.maxWidth, constraints.maxHeight)
        val density = LocalDensity.current
        val shape by remember(size, roundCorners, roundCornersSize) {
            derivedStateOf {
                if (roundCorners) {
                    if (roundCornersSize == null) RoundedCornerShape((size / 20).dp)
                    else RoundedCornerShape(roundCornersSize)
                } else RectangleShape
            }
        }
        val bitmap: ImageBitmap? by remember(image) {
            derivedStateOf {
                image?.let { painter ->
                    val intrinsicSize = painter.intrinsicSize
                    if (intrinsicSize.isSpecified) {
                        painter.toImageBitmap(
                            size = Size(
                                intrinsicSize.width,
                                intrinsicSize.height
                            ),
                            density = density
                        )
                    } else null
                }
            }
        }
        val background: Color by remember(bitmap) {
            derivedStateOf {
                if (useBackgroundFromPic) majorColor(bitmap)
                else backgroundColor ?: Color.Transparent
            }
        }
        val shadow: Color by remember(background) {
            derivedStateOf {
                if (useBackgroundFromPic) background.darker(shadowColorRatio)
                else shadowingColor ?: Color.Black
            }
        }
        val light: Color by remember(background) {
            derivedStateOf {
                if (useBackgroundFromPic) background.lighter(lightColorRatio)
                else lightColor ?: Color.White
            }
        }
        val border: BorderStroke by remember(background) {
            derivedStateOf {
                BorderStroke(
                    ((borderSize?.value ?: (size / 60f))).dp,
                    background
                )
            }
        }
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
                if (useShadows) OvalShadow(
                    modifier = Modifier.fillMaxSize(),
                    colorEnd = Color.Black,
                    colorStart = light,
                    ratio = lightRatio,
                    inverse = true
                )
                if (showImage) {
                    if (clearImageBackground) {
                        ChromedImage(
                            painter = image ?: EmptyPainter,
                            contentDescription = title,
                            contentScale = contentScale,
                            backgroundColor = background,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(imageScale)
                        )
                    } else {
                        Image(
                            painter = image ?: EmptyPainter,
                            contentDescription = title,
                            contentScale = contentScale,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(imageScale)
                        )
                    }
                }
                content?.invoke()
                if (useShadows) OvalShadow(
                    modifier = Modifier.fillMaxSize(),
                    colorEnd = Color.Transparent,
                    colorStart = shadow,
                    ratio = shadowRatio,
                    inverse = true
                )
                if (useShadows) Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            val gradient = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.5f to Color.Transparent,
                                    0.75f to shadow.copy(alpha = 0.5f),
                                    1.0f to shadow.copy(alpha = 0.9f)
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
                    if (title != null) Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

fun majorColor(
    bitmap: ImageBitmap? = null,
    defaultColor: Color? = null
): Color {
    val default = defaultColor ?: Color.Transparent
    if (bitmap == null) {
        return default
    } else {
        val androidBitmap = bitmap.asAndroidBitmap()
        val palette = Palette.from(androidBitmap).generate()
        val dominantInt = palette.getDominantColor(default.toArgb())
        return Color(dominantInt)
    }
}
