package org.mjdev.doorbellassistant.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import org.mjdev.doorbellassistant.enums.NotificationId
import org.mjdev.doorbellassistant.R
import org.mjdev.doorbellassistant.enums.ChannelId
import org.mjdev.doorbellassistant.helpers.MotionDetector
import java.util.concurrent.Executors
import kotlin.onFailure

class MotionDetectionService : LifecycleService() {
    private val motionDetector by lazy {
        MotionDetector(
            this,
            threshold = 0.05f,
            globalChangeThreshold = 0.75f,
            lightChangeThreshold = 75f
        )
    }
    private var cameraProvider: ProcessCameraProvider? = null
    private var currentRotation = Surface.ROTATION_0
    private val orientationListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                if (rotation != currentRotation) {
                    currentRotation = rotation
                    cameraProvider?.unbindAll()
                    bindCamera()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(
            NotificationId.MOTION.id,
            createNotification()
        )
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }
        initializeCamera()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        runCatching {
            cameraProvider?.unbindAll()
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            bindCamera()
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            cameraProvider?.unbindAll()
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            orientationListener.disable()
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            motionDetector.release()
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    private fun initializeCamera() = runCatching {
        ProcessCameraProvider.getInstance(this).apply {
            addListener({
                cameraProvider = get()
                bindCamera()
            }, ContextCompat.getMainExecutor(baseContext))
        }
    }.onFailure { e ->
        e.printStackTrace()
    }

    @Suppress("DEPRECATION")
    private fun bindCamera() = runCatching {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(Size(640, 480))
            .setTargetRotation(currentRotation)
            .build()
            .apply {
                setAnalyzer(Executors.newSingleThreadExecutor(), motionDetector)
            }
        cameraProvider?.bindToLifecycle(
            this,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            imageAnalysis
        )
    }.onFailure { e ->
        e.printStackTrace()
    }

    private fun createNotificationChannel() = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ChannelId.MOTION.id,
                "Motion Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }.onFailure { e ->
        e.printStackTrace()
    }

    private fun createNotification() = runCatching {
        NotificationCompat.Builder(this, ChannelId.MOTION.id)
            .setContentTitle("Monitoring front camera")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }.onFailure { e ->
        e.printStackTrace()
    }.getOrNull()

    companion object {
        fun start(context: Context) = runCatching {
            val intent = Intent(context, MotionDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }

        fun stop(context: Context) = runCatching {
            context.stopService(Intent(context, MotionDetectionService::class.java))
        }.onFailure { e ->
            e.printStackTrace()
        }
    }
}
