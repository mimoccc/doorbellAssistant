package org.mjdev.doorbellassistant.di

import android.content.Context
import org.kodein.di.DI
import org.kodein.di.LazyDI
import org.kodein.di.bindSingleton

fun mainDI(
    context: Context
): LazyDI = DI.lazy {
    bindSingleton<Context> { context }
    import(appModule)
}