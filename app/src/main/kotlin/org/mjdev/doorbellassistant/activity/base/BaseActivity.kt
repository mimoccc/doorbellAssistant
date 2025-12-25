package org.mjdev.doorbellassistant.activity.base

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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