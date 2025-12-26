package org.mjdev.doorbellassistant.activity.base

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import org.mjdev.doorbellassistant.BuildConfig
import org.mjdev.doorbellassistant.activity.AssistantActivity
import org.mjdev.doorbellassistant.activity.AssistantActivity.Companion.isMotionDetected
import org.mjdev.doorbellassistant.extensions.ComposeExt.enableEdgeToEdge
import org.mjdev.doorbellassistant.ui.theme.DarkMD5
import kotlin.jvm.java

open class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(DarkMD5, DarkMD5)
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 100)
        }
    }

    companion object {
        inline fun <reified T : BaseActivity> Context.isFinished(): Boolean {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val myPackage = BuildConfig.APPLICATION_ID
            val task = activityManager.appTasks.firstOrNull { task ->
                task.taskInfo.baseActivity?.packageName == myPackage &&
                        task.taskInfo.baseActivity?.className == T::class.qualifiedName
            }
            return task == null
        }

        inline fun <reified T : BaseActivity> Context.isRunning(): Boolean {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val myPackage = BuildConfig.APPLICATION_ID
            val task = activityManager.appTasks.firstOrNull { task ->
                task.taskInfo.baseActivity?.packageName == myPackage &&
                        task.taskInfo.baseActivity?.className == T::class.qualifiedName
            }
            return task != null
        }

        inline fun <reified T : BaseActivity> startOrResume(
            context: Context
        ) {
            isMotionDetected.value = true
            if (context.isFinished<T>()) {
                Intent(context, T::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                }.apply {
                    context.startActivity(this)
                }
            }
        }
    }
}