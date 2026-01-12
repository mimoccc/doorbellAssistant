package org.mjdev.phone.ui.theme.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.mjdev.phone.extensions.CustomExtensions.dashedBorder
import org.mjdev.phone.helpers.Previews

// todo
@Suppress("unused")
@Previews
@Composable
fun PhonePreviewBackground(
    isPreviewMode: Boolean = false,
    isEmptyTheme: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    if (isPreviewMode && isEmptyTheme) {
//        if (isEmptyTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(phoneColors.colorPreviewBackground),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .dashedBorder(Color.Gray)
            ) {
                content()
            }
        }
//        } else {
//            Box(
//                modifier = Modifier
//                    .wrapContentSize()
//                    .dashedBorder(Color.Gray)
//            ) {
//                content()
//            }
//        }
    } else {
        content()
    }
}