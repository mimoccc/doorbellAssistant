/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object StateExt {
    @Composable
    fun <T> produceStateInLifeCycleRepeated(
        initialValue: T,
        delayTime: Long = 1000L,
        key: Any,
        lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
        state: Lifecycle.State = Lifecycle.State.STARTED,
        block: suspend () -> T,
    ): State<T> = produceState(initialValue, key) {
        coroutineScope {
            launch(Dispatchers.IO) {
                lifecycle.repeatOnLifecycle(state) {
                    while (isActive) {
                        value = block()
                        delay(delayTime)
                    }
                }
            }
        }
    }

    @Composable
    fun <T> produceStateInLifeCycle(
        initialValue: T,
        key: Any,
        lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
        block: suspend () -> T,
    ): State<T> = produceState(initialValue, key) {
        coroutineScope {
            launch(Dispatchers.IO) {
                lifecycle.coroutineScope.launch {
                    value = block()
                }
            }
        }
    }
}
