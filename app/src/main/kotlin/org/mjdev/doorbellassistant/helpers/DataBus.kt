package org.mjdev.doorbellassistant.helpers

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("unused", "CanBeParameter")
open class DataBus<T : Any>(
    private val replay: Int = 0,
    private val extraBufferCapacity: Int = 64,
    private val onBufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
    private val scopeContext: CoroutineContext = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + scopeContext),
    private val config: DataBus<T>.() -> Unit = {}
) {
    private val _events = MutableSharedFlow<T>(
        replay,
        extraBufferCapacity,
        onBufferOverflow
    )

    val events: SharedFlow<T> = _events.asSharedFlow()

    init {
        config(this)
    }

    fun send(data: T): Job = scope.launch {
        _events.emit(data)
    }

    companion object {
        private val TAG = DataBus::class.java.simpleName

        fun <T : Any> DataBus<T>.subscribe(
            onError: (Throwable) -> Unit = {},
            onEvent: (T) -> Unit,
        ): Job = suspendSubscribe(
            onError = { e ->
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            },
            onEvent = { value ->
                withContext(Dispatchers.Main) {
                    onEvent(value)
                }
            }
        )

        fun <T : Any> DataBus<T>.suspendSubscribe(
            onError: suspend (Throwable) -> Unit = {},
            onEvent: suspend (T) -> Unit,
        ): Job = scope.launch {
            _events.onEach { value ->
                Log.d(TAG, "[Received $value")
            }.catch { e ->
                Log.e(TAG, "Error: ${e.message}", e)
                onError(e)
            }.collect { value ->
                onEvent(value)
            }
        }

        @Composable
        fun <T : Any> DataBus<T>.collectAsState(
            initial: T,
            context: CoroutineContext = EmptyCoroutineContext,
        ): State<T> = events.collectAsState(initial, context)
    }
}
