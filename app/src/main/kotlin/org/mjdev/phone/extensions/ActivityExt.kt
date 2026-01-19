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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.mjdev.phone.extensions.ColorExt.toColorInt

object ActivityExt {
    inline fun <reified T : Activity> Context.isFinished(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val myPackage = T::class.java.`package`?.name
        val task = activityManager.appTasks.firstOrNull { task ->
            task.taskInfo.baseActivity?.packageName == myPackage &&
                    task.taskInfo.baseActivity?.className == T::class.qualifiedName
        }
        return task == null
    }

    @SuppressLint("NewApi")
    inline fun <reified T : Activity> Context.isRunning(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val myPackage = T::class.java.`package`?.name?.replace(".activity", "")
        val task = activityManager.appTasks.firstOrNull { task ->
            task.taskInfo.baseActivity?.packageName == myPackage &&
                    task.taskInfo.baseActivity?.className == T::class.qualifiedName
        }
        return task != null && task.taskInfo.isVisible
    }

    inline fun <reified T : Activity> Context.startOrResume() {
        if (isFinished<T>()) {
            Intent(this, T::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            }.apply {
                startActivity(this)
            }
        } else {
            bringToFront<T>()
        }
    }

    fun ComponentActivity.enableEdgeToEdge(
        statusBarColor: Color = Color.DarkGray,
        navigationBarColor: Color = Color.DarkGray,
    ) = enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(statusBarColor.toColorInt()),
        navigationBarStyle = SystemBarStyle.dark(navigationBarColor.toColorInt())
    )

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    fun Activity.setFullScreen() {
        runCatching {
            setTurnScreenOn(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setShowWhenLocked(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    @SuppressLint("NewApi")
    fun Activity.turnDisplayOn() {
        runCatching {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setTurnScreenOn(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setShowWhenLocked(true)
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    fun Activity.hideSystemBars() = runCatching {
        WindowCompat.getInsetsController(window, window.decorView).also { windowInsetsController ->
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }.onFailure { e ->
        e.printStackTrace()
    }

    @SuppressLint("NewApi")
    fun Activity.turnDisplayOff() {
        runCatching {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            @Suppress("DEPRECATION")
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setTurnScreenOn(false)
        }.onFailure { e ->
            e.printStackTrace()
        }
        runCatching {
            setShowWhenLocked(false)
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    inline fun <reified T : Activity> Context.bringToFront() {
        runCatching {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val myPackage = T::class.java.`package`?.name
            val task = activityManager.appTasks.firstOrNull { task ->
                task.taskInfo.baseActivity?.packageName == myPackage
            }
            task?.moveToFront()
        }.onFailure { e ->
            e.printStackTrace()
        }
    }
}
