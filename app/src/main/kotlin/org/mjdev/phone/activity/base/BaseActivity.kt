package org.mjdev.phone.activity.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.activity.compose.setContent as overrideSetContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.mjdev.phone.extensions.CustomExtensions.enableEdgeToEdge
import org.mjdev.phone.ui.theme.base.PhoneTheme

open class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // todo bck color
        enableEdgeToEdge(
            Color.Transparent,
            Color.Transparent
        )
        super.onCreate(savedInstanceState)
    }

    fun setContent(
        parent: CompositionContext? = null,
        content: @Composable () -> Unit,
    ) = overrideSetContent(parent, content = {
        PhoneTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .displayCutoutPadding()
            ) {
                content()
            }
        }
    })
}
