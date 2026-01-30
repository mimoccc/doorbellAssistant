/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.stt.transcribers.base

import android.content.Context
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.doorbellassistant.helpers.FileDownloader
import org.mjdev.phone.helpers.DataBus
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ITKit(
    context: Context,
    scopeContext: CloseableCoroutineDispatcher,
    scope: CoroutineScope
) : DataBus<ITKitResult>(
    scopeContext = scopeContext,
    scope = scope
) {
    private val downloader by lazy {
        FileDownloader(context)
    }

    abstract fun setModel(modelType: ITKitModel)
    abstract fun init()
    abstract fun release()
    abstract fun transcribe(data: ByteArray)
    abstract fun subscribe(
        onError: (Throwable) -> Unit = {},
        onEvent: (ITKitResult) -> Unit,
    )

    fun downloadToFile(
        url: String,
        file: File,
        description: String = "Downloading ${url.substringAfterLast("/")} ...",
        onProgress: ((file: File, percent: Float) -> Unit)? = null,
        onComplete: ((success: Boolean, file: File?) -> Unit)? = null,
    ) = downloader.downloadFileAndCopyTo(
        url = url,
        file = file,
        description = description,
        onProgress = onProgress,
        onComplete = onComplete,
    )
}
