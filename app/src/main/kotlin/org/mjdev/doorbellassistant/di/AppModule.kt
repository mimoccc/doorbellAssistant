package org.mjdev.doorbellassistant.di

import android.app.Application
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import okhttp3.Cache
import okhttp3.logging.HttpLoggingInterceptor
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.mjdev.doorbellassistant.BuildConfig
import org.mjdev.doorbellassistant.extensions.ComposeExt.isInPreviewMode
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
val appModule = DI.Module("appModule") {
    // providers
    bindProvider<Application> {
        val context = instance<Context>()
        (context.applicationContext as? Application)
            ?: error("Context is not an Application instance.")
    }
//    bindProvider<PreferencesManager> {
//        PreferencesManager(
//            context = instance()
//        ).setName("app_preferences")
//            .setMode(MODE_PRIVATE)
//            .init()
//    }
    bindProvider<Cache> {
        val context: Context = instance()
        val systemCachePath = System.getProperty("java.io.tmpdir")
        val systemCacheDir = File(systemCachePath, "http_cache")
        val cacheDir = runCatching {
            context.cacheDir?.let {
                File(it, "http_cache")
            }
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull() ?: systemCacheDir
        Cache(
            directory = cacheDir,
            maxSize = 1024L * 1024L * 1024L // 1GB
        )
    }
    bindProvider<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            setLevel(
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            )
        }
    }
    bindProvider<OkHttpClient> {
        val logging: HttpLoggingInterceptor = instance()
        if (isInPreviewMode) {
            OkHttpClient.Builder()
                .followRedirects(true)
                .addInterceptor(logging)
                .build()
        } else {
            OkHttpClient.Builder()
                .cache(instance())
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .callTimeout(60000, TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .addInterceptor(logging)
                .build()
        }
    }
    // singletons
    bindSingleton<ConnectivityManager> {
        instance<Context>()
            .getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    bindSingleton<NotificationManager> {
        instance<Context>()
            .getSystemService(NotificationManager::class.java) as NotificationManager
    }
    bindSingleton<WindowManager> {
        instance<Context>()
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    bindSingleton<KeyguardManager> {
        instance<Context>()
            .getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }
    bindSingleton<CoroutineScope> {
        CoroutineScope(Dispatchers.IO + Job())
    }
}
