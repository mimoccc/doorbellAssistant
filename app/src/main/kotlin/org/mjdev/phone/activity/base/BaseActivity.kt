/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.activity.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.mjdev.phone.extensions.ActivityExt.enableEdgeToEdge
import org.mjdev.phone.ui.components.PermissionsScreen
import org.mjdev.phone.ui.theme.base.PhoneTheme
import androidx.activity.compose.setContent as overrideSetContent

open class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // todo bck color, phoneColors not available yet
        enableEdgeToEdge(
            Color.Transparent,
            Color.Transparent
        )
        super.onCreate(savedInstanceState)
    }

    fun setContent(
        parent: CompositionContext? = null,
        permissionsScreen: @Composable () -> Unit = {},
        content: @Composable () -> Unit,
    ) = overrideSetContent(parent, content = {
        PhoneTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .displayCutoutPadding()
            ) {
                PermissionsScreen(
                    permissionsInfoContent = permissionsScreen,
                    content = content
                )
            }
        }
    })
}
