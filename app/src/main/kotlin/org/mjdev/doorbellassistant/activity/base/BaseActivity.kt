package org.mjdev.doorbellassistant.activity.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import org.mjdev.doorbellassistant.extensions.ComposeExt.enableEdgeToEdge
import org.mjdev.doorbellassistant.ui.theme.DarkMD5

open class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(DarkMD5, DarkMD5)
        super.onCreate(savedInstanceState)
    }
}