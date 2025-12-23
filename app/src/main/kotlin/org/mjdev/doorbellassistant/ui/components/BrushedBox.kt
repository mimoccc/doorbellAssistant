package org.mjdev.doorbellassistant.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import org.mjdev.doorbellassistant.helpers.Previews
import org.mjdev.doorbellassistant.ui.theme.DarkMD5
import org.mjdev.doorbellassistant.ui.theme.DoorBellAssistantTheme
import kotlin.math.absoluteValue

// todo better brush due orientation & image size / video size
@SuppressLint("ConfigurationScreenWidthHeight")
@Previews
@Composable
fun BrushedBox(
    modifier: Modifier = Modifier,
    brushRatioLandscape: Float = 0.99f,
    brushRatioPortrait: Float = 1.3f,
) = DoorBellAssistantTheme {
    val config = LocalConfiguration.current
    val brush by remember(config) {
        derivedStateOf {
            when (config.orientation) {
                ORIENTATION_LANDSCAPE -> Brush.radialGradient(
                    colors = listOf(
                        Transparent,
                        Transparent,
                        Transparent,
                        Transparent,
                        Transparent,
                        DarkMD5,
                    ),
                    radius = config.screenHeightDp.absoluteValue * brushRatioLandscape
                )

                else -> Brush.radialGradient(
                    colors = listOf(
                        Transparent,
                        Transparent,
                        Transparent,
                        Transparent,
                        Transparent,
                        DarkMD5,
                    ),
                    radius = config.screenWidthDp.absoluteValue * brushRatioPortrait
                )
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = brush,
                shape = RectangleShape,
            )
    )
}
