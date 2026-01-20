# Phone Module ProGuard Rules
# Keep all public classes and members in the phone package
-keep public class org.mjdev.phone.** {
    public *;
}
# Keep WebRTC classes and native methods
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**
# Keep Ktor classes
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
# Keep Kotlin serialization classes
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**
# Keep Kodein DI classes
-keep class org.kodein.di.** { *; }
-dontwarn org.kodein.di.**
# Keep OkHttp classes
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
# Keep Gson classes
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
# Keep Bouncy Castle classes
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
# Keep Conscrypt classes
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**
# Keep AndroidX classes that might be used
-keep class androidx.** { *; }
-dontwarn androidx.**
# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# Keep annotation classes
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
