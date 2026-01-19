/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.ui.theme.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.mjdev.phone.extensions.ModifierExt.dashedBorder
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