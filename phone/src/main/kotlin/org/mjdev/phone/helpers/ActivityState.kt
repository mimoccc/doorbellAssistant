/*
 *
 *  * Copyright (c) Milan Jurkulák 2026.
 *
 *  * Contact:
 *  * e: mimoccc@gmail.com
 *  * e: mj@mjdev.org
 *  * w: https://mjdev.org
 *  * w: https://github.com/mimoccc
 *  * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 *
 *
 */

package org.mjdev.phone.helpers

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class ActivityState(
    val activity: Activity?
) {
    private val _isResumed: MutableState<Boolean> = mutableStateOf(false)

    val isResumed
        get() = _isResumed.value

    val isPaused
        get() = isResumed.not()

    @Composable
    fun ObserveLifecycle() {
        val lifecycleOwner = activity as? LifecycleOwner ?: return
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> _isResumed.value = true
                    Lifecycle.Event.ON_PAUSE -> _isResumed.value = false
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    companion object {
        @Composable
        fun rememberActivityState(): ActivityState {
            val activity: Activity? = LocalActivity.current
            val state = remember(activity) {
                ActivityState(activity)
            }
            state.ObserveLifecycle()
            return state
        }
    }
}