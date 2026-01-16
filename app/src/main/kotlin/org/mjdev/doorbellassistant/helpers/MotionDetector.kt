package org.mjdev.doorbellassistant.helpers

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.mjdev.doorbellassistant.enums.IntentAction
import org.mjdev.doorbellassistant.receiver.MotionBroadcastReceiver
import kotlin.math.abs

class MotionDetector(
    private val context: Context,
    private val threshold: Float = 0.05f,
    private val globalChangeThreshold: Float = 0.75f,
    private val lightChangeThreshold: Float = 50f
) : ImageAnalysis.Analyzer, SensorEventListener {
    private var previousFrame: Bitmap? = null
    private var lastDetectionTime = 0L
    private val detectionCooldown = 3000L
    private var currentLightLevel = 0f
    private var previousLightLevel = 0f
    private var lightChangeTimestamp = 0L
    private val lightChangeCooldown = 2000L

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    init {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)?.let { lightSensor ->
            sensorManager.registerListener(
                this,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_LIGHT) {
                val newLevel = it.values[0]
                val diff = abs(newLevel - currentLightLevel)
                if (diff > lightChangeThreshold) {
                    previousLightLevel = currentLightLevel
                    lightChangeTimestamp = System.currentTimeMillis()
                }
                currentLightLevel = newLevel
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
    }

    override fun analyze(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val current = rotateImageByOrientation(
            imageProxy.toBitmap(),
            rotationDegrees,
        )
        previousFrame?.let { previous ->
            val currentTime = System.currentTimeMillis()
            if (wasRecentLightChange(currentTime)) {
                previousFrame = current
                imageProxy.close()
                return
            }
            val changeData = analyzeFrameChange(previous, current)
            if (changeData != null) {
                latestBitmap.value = current
                if (shouldTriggerMotion(changeData, currentTime)) {
                    lastDetectionTime = currentTime
                    context.sendMotionIntent(true)
                }
            }
        }
        previousFrame = current
        imageProxy.close()
    }

    private fun wasRecentLightChange(
        currentTime: Long
    ): Boolean = currentTime - lightChangeTimestamp < lightChangeCooldown

    private fun rotateImageByOrientation(
        bitmap: Bitmap,
        rotationDegrees: Int,
    ): Bitmap = if (rotationDegrees == 0) bitmap else Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        },
        true
    )

    private fun analyzeFrameChange(
        frame1: Bitmap,
        frame2: Bitmap
    ): ChangeData? = runCatching {
        val pixels1 = IntArray(frame1.width * frame1.height)
        val pixels2 = IntArray(frame2.width * frame2.height)
        frame1.getPixels(pixels1, 0, frame1.width, 0, 0, frame1.width, frame1.height)
        frame2.getPixels(pixels2, 0, frame2.width, 0, 0, frame2.width, frame2.height)
        var totalDiff = 0L
        var significantChanges = 0
        val pixelThreshold = 40
        pixels1.indices.forEach { i ->
            val diff = abs(Color.red(pixels1[i]) - Color.red(pixels2[i])) +
                    abs(Color.green(pixels1[i]) - Color.green(pixels2[i])) +
                    abs(Color.blue(pixels1[i]) - Color.blue(pixels2[i]))
            totalDiff += diff
            if (diff > pixelThreshold) {
                significantChanges++
            }
        }
        val avgDiff = totalDiff.toFloat() / (pixels1.size * 765f)
        val changePercent = significantChanges.toFloat() / pixels1.size
        return ChangeData(avgDiff, changePercent)
    }.getOrNull()

    private fun shouldTriggerMotion(
        changeData: ChangeData,
        currentTime: Long
    ): Boolean {
        if (currentTime - lastDetectionTime < detectionCooldown) return false
        if (changeData.averageDifference < threshold) return false
        if (changeData.percentageChanged > globalChangeThreshold) return false
        return true
    }

    fun release() {
        sensorManager.unregisterListener(this)
    }

    data class ChangeData(
        val averageDifference: Float,
        val percentageChanged: Float
    )

    companion object {
        val latestBitmap: MutableState<Bitmap?> = mutableStateOf(null)

        fun Context.sendMotionIntent(isMotionDetected: Boolean) {
            Intent(
                applicationContext,
                MotionBroadcastReceiver::class.java
            ).apply {
                action = if (isMotionDetected) IntentAction.MOTION_DETECTED.action
                else IntentAction.MOTION_LOST.action
                applicationContext.sendBroadcast(this)
            }
        }
    }
}
