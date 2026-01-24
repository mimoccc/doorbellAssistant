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

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import org.mjdev.phone.extensions.ComposeExt.rememberAssetImagePainter
import org.mjdev.phone.extensions.CustomExt.toImageBitmap
import org.mjdev.phone.helpers.Previews

@Previews
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ChromedImage(
    painter: Painter= rememberAssetImagePainter("avatar/avatar_yellow.png"),
    contentDescription: String?="",
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    backgroundColor: Color= Color.Transparent,
    clearColor :Color = Color(0xFFFF9500),
    threshold: Float = 0.15f,
    colorFilter: ColorFilter? = null, // todo
) = BoxWithConstraints(
    modifier = modifier.background(backgroundColor)
) {
    val density = LocalDensity.current
    val bitmap: ImageBitmap? by remember(painter) {
        derivedStateOf {
            painter.toImageBitmap(
                size = Size(
                    constraints.maxWidth.toFloat(),
                    constraints.maxHeight.toFloat()),
                density = density,
            )
        }
    }
    val shader = remember(bitmap) {
        RuntimeShader(
            """
            uniform shader inputShader;
            uniform vec4 targetColor;
            uniform float threshold;
            half4 main(float2 fragCoord) {
                half4 color = inputShader.eval(fragCoord);
                float dist = distance(color.rgb, targetColor.rgb);
                float alpha = smoothstep(threshold, threshold + 0.05, dist);
                return vec4(color.rgb, color.a * alpha);
            }
        """
        )
    }
    Canvas(
        modifier = modifier
            .clipToBounds()
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                    this.role = Role.Image
                }
            }
    ) {
        val srcSize = Size(
            bitmap?.width?.toFloat()?:1f,
            bitmap?.height?.toFloat() ?: 1f
        )
        val scaleFactor = contentScale.computeScaleFactor(srcSize, size)
        val scaledWidth = srcSize.width * scaleFactor.scaleX
        val scaledHeight = srcSize.height * scaleFactor.scaleY
        val dx = (size.width - scaledWidth) / 2f
        val dy = (size.height - scaledHeight) / 2f
        val bitmapShader = ImageShader(bitmap ?: ImageBitmap(1,1))
        shader.setInputShader("inputShader", bitmapShader)
        shader.setFloatUniform(
            "targetColor",
            clearColor.red,
            clearColor.green,
            clearColor.blue,
            clearColor.alpha
        )
        shader.setFloatUniform("threshold", threshold)
        withTransform({
            translate(dx, dy)
            scale(scaleFactor.scaleX, scaleFactor.scaleY, pivot = Offset.Zero)
        }) {
            drawRect(
                brush = ShaderBrush(shader),
                size = srcSize,
                colorFilter = colorFilter
            )
        }
    }
}
