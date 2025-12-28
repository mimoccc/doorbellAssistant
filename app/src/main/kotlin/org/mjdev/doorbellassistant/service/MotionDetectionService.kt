package org.mjdev.doorbellassistant.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import org.mjdev.doorbellassistant.R
import org.mjdev.doorbellassistant.enums.ChannelId
import org.mjdev.doorbellassistant.enums.NotificationId
import org.mjdev.doorbellassistant.helpers.MotionDetector
import java.util.concurrent.Executors

@Suppress("unused")
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
        isRunning.value = true
        startAsForeground()
        super.onCreate()
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }
        initializeCamera()
    }

    override fun onConfigurationChanged(
        newConfig: Configuration
    ) {
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
        isRunning.value = false
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

    @SuppressLint("InlinedApi")
    private fun startAsForeground() {
        createNotificationChannel()
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationId.MOTION.id,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
        } else {
            startForeground(NotificationId.MOTION.id, notification)
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
        val channel = NotificationChannel(
            ChannelId.MOTION.id,
            "Motion Detection",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
            setShowBadge(false)
        }
        getSystemService(
            NotificationManager::class.java
        )?.createNotificationChannel(channel)
    }.onFailure { e ->
        e.printStackTrace()
    }

    private fun createNotification() = runCatching {
        NotificationCompat.Builder(this, ChannelId.MOTION.id)
            .setContentTitle("Monitoring front camera")
            .setContentText("Running...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }.onFailure { e ->
        e.printStackTrace()
    }.getOrElse {
        NotificationCompat.Builder(
            this,
            ChannelId.MOTION.id
        ).setSmallIcon(
            R.mipmap.ic_launcher
        ).build()
    }

    companion object {
        private val TAG = MotionDetectionService::class.simpleName
        private val isRunning = mutableStateOf(false)

        fun start(context: Context) = runCatching {
            if (isRunning.value.not()) Intent(
                context,
                MotionDetectionService::class.java
            ).also { intent ->
                context.startForegroundService(intent)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }

        fun stop(context: Context) = runCatching {
            if (isRunning.value) Intent(
                context,
                MotionDetectionService::class.java
            ).also { intent ->
                context.stopService(intent)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
    }
}
