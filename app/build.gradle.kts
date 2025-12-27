import SafeMap.Companion.toSafeMap
import kotlin.apply
import kotlin.collections.forEach
import kotlin.reflect.KProperty

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
}

class SafeMap : HashMap<String, String>() {
    companion object {
        @Suppress("USELESS_ELVIS")
        operator fun SafeMap.getValue(
            thisRef: Any?,
            property: KProperty<*>
        ): String = get(property.name) ?: ""

        fun List<Pair<String, String>>.toSafeMap() = SafeMap().apply {
            this@toSafeMap.forEach { (key, value) -> put(key, value) }
        }
    }

    override operator fun get(
        key: String
    ): String = runCatching { super.get(key) }.getOrNull() ?: ""
}

fun Project.readPropsFile(
    relativePath: String
): SafeMap = runCatching {
    val file = rootDir.resolve(relativePath)
    println("Reading props file: $file")
    file.readLines().mapNotNull { line ->
        runCatching {
            line.trim().split("=").let { ss ->
                if (ss.size == 2) {
                    Pair(ss[0].trim(), ss[1].trim())
                } else null
            }
        }.onFailure { e -> println("e: $e") }.getOrNull()
    }.toSafeMap().apply {
        forEach { p ->
            val key = p.key
            val value = if (key.contains("pass", true)) "******" else p.value
        }
    }
}.getOrNull() ?: SafeMap()

android {
    namespace = "org.mjdev.doorbellassistant"

    compileSdk {
        version = release(36)
    }

    packaging {
        resources {
            excludes += "META-INF/mailcap"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/NOTICE.md"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/INDEX.LIST"
        }
    }

    defaultConfig {
        applicationId = "org.mjdev.doorbellassistant"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        readPropsFile("stream-api-secrets.txt").also { streamSecrets ->
            buildConfigField(
                "String",
                "STREAM_API_KEY",
                "\"" + streamSecrets["apiKey"] + "\""
            )
            buildConfigField(
                "String",
                "STREAM_API_USER_TOKEN",
                "\"" + streamSecrets["userToken"] + "\""
            )
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.ui)

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.camera.lifecycle)

    implementation(libs.firebase.ai)
    implementation(libs.accompanist.permissions)
    implementation(libs.generativeai)

    implementation(libs.stream.video.android.core)
    implementation(libs.stream.video.android.ui.compose)
    implementation(libs.stream.video.android.ui.core)
    implementation(libs.stream.video.android.filters.video)
    implementation(libs.stream.video.android.previewdata)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
//    implementation(libs.gson)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
