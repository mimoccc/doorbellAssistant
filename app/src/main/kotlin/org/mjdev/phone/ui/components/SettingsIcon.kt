package org.mjdev.phone.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phoneIcons

@Previews
@Composable
fun SettingsIcon(
    modifier: Modifier = Modifier,
    onClickSettings: () -> Unit = {},
    shape: Shape = CircleShape,
) = PhoneTheme {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .clickable(onClick = onClickSettings),
            contentDescription = "",
            painter = phoneIcons.buttonSettings,
            tint = phoneColors.colorIconTint,
        )
    }
}