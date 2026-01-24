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

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors

@Previews
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissableModalSheet(
    isShown: Boolean = true,
    onDismissRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    sheetGesturesEnabled: Boolean = true,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 0.dp,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
    content: @Composable ColumnScope.() -> Unit = {},
) = PhoneTheme {
    var shouldBeVisible by remember { mutableStateOf(isShown) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    if (shouldBeVisible) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = {
                shouldBeVisible = false
                onDismissRequest()
            },
            sheetState = sheetState,
            sheetGesturesEnabled = sheetGesturesEnabled,
            containerColor = containerColor,
            contentColor = contentColor,
            shape = shape,
            scrimColor = scrimColor,
            tonalElevation = tonalElevation,
            dragHandle = dragHandle,
            contentWindowInsets = contentWindowInsets,
            properties = properties,
            content = {
                SetBarsOnSheetDialogWindow(
                    navigationBarColor = phoneColors.colorBackground,
                    lightNavIcons = false
                )
                content()
            }
        )
    }
}

@Suppress("DEPRECATION")
@Composable
private fun SetBarsOnSheetDialogWindow(
    navigationBarColor: Color,
    lightNavIcons: Boolean,
) {
    val view = LocalView.current
    DisposableEffect(navigationBarColor, lightNavIcons) {
        val dialogWindow = (view.parent as? DialogWindowProvider)?.window
        if (dialogWindow != null) {
            dialogWindow.decorView.setBackgroundDrawable(0.toDrawable())
            val oldColor = dialogWindow.navigationBarColor
            val controller = WindowCompat.getInsetsController(dialogWindow, view)
            val oldIcons = controller.isAppearanceLightNavigationBars
            dialogWindow.navigationBarColor = navigationBarColor.toArgb()
            controller.isAppearanceLightNavigationBars = lightNavIcons
            onDispose {
                dialogWindow.navigationBarColor = oldColor
                controller.isAppearanceLightNavigationBars = oldIcons
            }
        } else {
            onDispose { }
        }
    }
}