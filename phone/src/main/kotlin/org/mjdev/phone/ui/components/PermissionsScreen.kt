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

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.mjdev.phone.extensions.PermissionsExt.LaunchPermissions
import org.mjdev.phone.ui.theme.base.PhoneTheme

@Composable
fun PermissionsScreen(
    permissionsInfoContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) = PhoneTheme {
    val activity = LocalActivity.current
    var state by remember { mutableStateOf(false) }
    if (state.not()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            permissionsInfoContent()
        }
    } else {
        content()
    }
    LaunchPermissions(
        onPermissionsResult = { pms ->
            state = pms.any { p -> p.value }
            if (state.not()) {
                activity?.recreate()
            }
        },
        onAllPermissionsGranted = {
            state = true
        }
    )
}