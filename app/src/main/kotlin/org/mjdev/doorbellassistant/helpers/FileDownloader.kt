/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.helpers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.io.File

@Suppress("unused")
class FileDownloader(
    private val context: Context
) {
    companion object {
        private val TAG = FileDownloader::class.simpleName
    }

    private val downloadManager: DownloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    private val downloadReceivers = mutableMapOf<Long, BroadcastReceiver>()

    private val applicationContext
        get() = context.applicationContext

    fun downloadFile(
        url: String,
        fileName: String? = null,
        description: String = "Downloading $fileName ...",
        subPath: String? = null,
        mimeType: String? = null,
        requiresCharging: Boolean = false,
        allowedOverMetered: Boolean = true,
        allowedOverRoaming: Boolean = true,
        showNotification: Boolean = true,
        onComplete: ((success: Boolean, uri: Uri?) -> Unit)? = null
    ): Long {
        val uri = url.toUri()
        val request = DownloadManager.Request(uri).apply {
            val finalFileName = fileName
                ?: uri.lastPathSegment
                ?: "download_${System.currentTimeMillis()}"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    if (subPath != null) "$subPath/$finalFileName" else finalFileName
                )
            } else {
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    if (subPath != null) "$subPath/$finalFileName" else finalFileName
                )
            }
            val detectedMimeType = mimeType ?: getMimeType(finalFileName)
            detectedMimeType?.let { setMimeType(it) }
            setTitle(finalFileName)
            setDescription(description)
            if (showNotification) {
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            } else {
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            }
            if (allowedOverMetered) {
                setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                )
            } else {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            }
            setAllowedOverRoaming(allowedOverRoaming)
            setRequiresCharging(requiresCharging)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    if (subPath != null) "$subPath/$finalFileName" else finalFileName
                )
            }
        }
        val downloadId = downloadManager.enqueue(request)
        if (onComplete != null) {
            registerDownloadReceiver(downloadId, onComplete)
        }
        return downloadId
    }

    fun getDownloadStatus(
        downloadId: Long
    ): DownloadStatus {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor? = downloadManager.query(query)
        return cursor?.use {
            if (it.moveToFirst()) {
                val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val reasonIndex = it.getColumnIndex(DownloadManager.COLUMN_REASON)
                val bytesDownloadedIndex =
                    it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val bytesTotalIndex = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val status = it.getInt(statusIndex)
                val reason = if (reasonIndex >= 0) it.getInt(reasonIndex) else 0
                val bytesDownloaded =
                    if (bytesDownloadedIndex >= 0) it.getLong(bytesDownloadedIndex) else 0L
                val bytesTotal = if (bytesTotalIndex >= 0) it.getLong(bytesTotalIndex) else 0L
                DownloadStatus(
                    id = downloadId,
                    status = status,
                    reason = reason,
                    bytesDownloaded = bytesDownloaded,
                    bytesTotal = bytesTotal,
                    progress = if (bytesTotal > 0) (bytesDownloaded * 100 / bytesTotal).toInt() else 0
                )
            } else {
                DownloadStatus(
                    downloadId,
                    DownloadManager.STATUS_FAILED,
                    0,
                    0,
                    0,
                    0
                )
            }
        } ?: DownloadStatus(
            downloadId,
            DownloadManager.STATUS_FAILED,
            0,
            0,
            0,
            0
        )
    }

    fun getDownloadUri(
        downloadId: Long
    ): Uri? = downloadManager.getUriForDownloadedFile(downloadId)

    fun cancelDownload(
        downloadId: Long
    ) {
        downloadManager.remove(downloadId)
        unregisterDownloadReceiver(downloadId)
    }

    private fun registerDownloadReceiver(
        downloadId: Long,
        onComplete: (success: Boolean, uri: Uri?) -> Unit
    ) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    val status = getDownloadStatus(downloadId)
                    val success = status.status == DownloadManager.STATUS_SUCCESSFUL
                    val uri = if (success) getDownloadUri(downloadId) else null
                    if (success) {
                        Log.d(TAG, "Download completed: id=$downloadId, success=$success, uri=$uri")
                        onComplete(success, uri)
                        unregisterDownloadReceiver(downloadId)
                    }
                }
            }
        }
        downloadReceivers[downloadId] = receiver
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                applicationContext,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    private fun unregisterDownloadReceiver(
        downloadId: Long
    ) {
        downloadReceivers[downloadId]?.let { receiver ->
            try {
                applicationContext.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // no op
            }
            downloadReceivers.remove(downloadId)
        }
    }

    private fun getMimeType(
        fileName: String
    ): String? {
        val extension = fileName.substringAfterLast('.', "")
        return if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        } else {
            null
        }
    }

    fun downloadFileAndCopyTo(
        url: String,
        file: File,
        description: String = "Downloading ${file.name}...",
        requiresCharging: Boolean = false,
        allowedOverMetered: Boolean = true,
        allowedOverRoaming: Boolean = true,
        showNotification: Boolean = true,
        onProgress: ((file: File, percent: Float) -> Unit)? = null,
        onComplete: ((success: Boolean, file: File?) -> Unit)? = null
    ) {
        Log.d(TAG, "Starting download: url=$url, target=${file.absolutePath}")
        val uri = url.toUri()
        val request = DownloadManager.Request(uri).apply {
            val finalFileName = file.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    finalFileName
                )
            } else {
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    finalFileName
                )
            }
            val detectedMimeType = getMimeType(finalFileName)
            detectedMimeType?.let { setMimeType(it) }
            setTitle(finalFileName)
            setDescription(description)
            setRequiresCharging(requiresCharging)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
            setAllowedOverRoaming(allowedOverRoaming)
            setAllowedOverMetered(allowedOverMetered)
        }
        val downloadId = downloadManager.enqueue(request)
        Log.d(TAG, "Download started with ID: $downloadId")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    Log.d(TAG, "Download completed for ID: $downloadId")
                    val status = getDownloadStatus(downloadId)
                    val success = status.status == DownloadManager.STATUS_SUCCESSFUL
                    Log.d(TAG, "Download status: success=$success")
                    if (success) {
                        val downloadedUri = getDownloadUri(downloadId)
                        if (downloadedUri != null) {
                            Log.d(TAG, "Copying file from $downloadedUri to ${file.absolutePath}")
                            val copySuccess = copyDownloadedFileToAppStorage(downloadedUri, file)
                            Log.d(
                                TAG,
                                "Copy result: success=$copySuccess, file.exists=${file.exists()}, file.length=${file.length()}"
                            )
                            if (copySuccess && file.exists() && file.length() > 0) {
                                Log.d(TAG, "File successfully copied to ${file.absolutePath}")
                                onComplete?.invoke(true, file)
                            } else {
                                Log.e(
                                    TAG,
                                    "Copy failed or file is invalid. copySuccess=$copySuccess, exists=${file.exists()}, size=${file.length()}"
                                )
                                onComplete?.invoke(false, null)
                            }
                        } else {
                            Log.e(TAG, "Cannot get URI for downloaded file. downloadId=$downloadId")
                            onComplete?.invoke(false, null)
                        }
                    } else {
                        Log.e(
                            TAG,
                            "Download failed. Status: ${status.status}, Reason: ${status.reason}"
                        )
                        onComplete?.invoke(false, null)
                    }
                    unregisterDownloadReceiver(downloadId)
                }
            }
        }
        downloadReceivers[downloadId] = receiver
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                applicationContext,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
        if (onProgress != null) {
            startProgressMonitoring(downloadId, file, onProgress)
        }
    }

    private fun startProgressMonitoring(
        downloadId: Long,
        file: File,
        onProgress: ((file: File, percent: Float) -> Unit)? = null
    ) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val status = getDownloadStatus(downloadId)
                val percent = status.progress / 100f
                onProgress?.invoke(file, percent)
                Log.d(TAG, "Download progress: ${percent * 100} %")
                if (status.isRunning || status.isPending) {
                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(runnable)
    }

    private fun copyDownloadedFileToAppStorage(
        sourceUri: Uri,
        targetFile: File
    ): Boolean = try {
        Log.d(TAG, "Attempting to copy from $sourceUri to ${targetFile.absolutePath}")
        targetFile.parentFile?.let { parent ->
            if (!parent.exists()) {
                val created = parent.mkdirs()
                Log.d(TAG, "Created parent directory: ${parent.absolutePath}, success=$created")
            }
        }
        var copied = false
        applicationContext.contentResolver.openInputStream(sourceUri)?.use { input ->
            targetFile.outputStream().use { output ->
                val bytes = input.copyTo(output)
                Log.d(TAG, "Copied $bytes bytes")
                copied = true
            }
        }
        if (!copied) {
            Log.e(TAG, "Failed to open input stream from $sourceUri")
        }
        copied && targetFile.exists() && targetFile.length() > 0
    } catch (e: Exception) {
        Log.e(TAG, "Error copying file: ${e.message}", e)
        e.printStackTrace()
        false
    }

    fun cleanup() {
        downloadReceivers.keys.toList().forEach { downloadId ->
            unregisterDownloadReceiver(downloadId)
        }
    }

    data class DownloadStatus(
        val id: Long,
        val status: Int,
        val reason: Int,
        val bytesDownloaded: Long,
        val bytesTotal: Long,
        val progress: Int
    ) {
        val isRunning: Boolean
            get() = status == DownloadManager.STATUS_RUNNING
        val isPending: Boolean
            get() = status == DownloadManager.STATUS_PENDING
        val isSuccessful: Boolean
            get() = status == DownloadManager.STATUS_SUCCESSFUL
        val isFailed: Boolean
            get() = status == DownloadManager.STATUS_FAILED
        val isPaused: Boolean
            get() = status == DownloadManager.STATUS_PAUSED
    }
}
