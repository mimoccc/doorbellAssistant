package org.mjdev.phone.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.mjdev.phone.extensions.CustomExtensions.neonStroke
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.phoneColors

@Suppress("LocalVariableName")
@Previews
@Composable
fun GlowButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = phoneColors.background,
    glowColor: Color = Color.White,
    glowRadius: Float = 8f,
    onClick: () -> Unit = {},
    shape: Shape = CircleShape,
    animate: Boolean = true,
    animMaxGlow: Float = 16f,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = if (animate) rememberInfiniteTransition(label = "glow") else null
    val _glowRadius = infiniteTransition?.animateFloat(
        initialValue = glowRadius,
        targetValue = animMaxGlow,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )?.value ?: glowRadius
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .neonStroke(
                backgroundColor = backgroundColor,
                glowColor = glowColor,
                glowRadius = _glowRadius,
                shape = shape
            ),
        shape = shape,
        colors = buttonColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = modifier
                .alpha(1 / animMaxGlow * _glowRadius)
                .scale(1 / animMaxGlow * _glowRadius)
        ) {
            content()
        }
    }
}
