@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
}
android {
    namespace = "org.mjdev.tts"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
    }
    sourceSets {
        getByName("main") {
            @Suppress("DEPRECATION")
            java.srcDirs("java")
            @Suppress("DEPRECATION")
            kotlin.srcDirs("kotlin")
            manifest.srcFile("AndroidManifest.xml")
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }
    externalNativeBuild {
        cmake {
            path("cpp/CMakeLists.txt")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
dependencies {
    implementation(libs.onnxruntime.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
}
