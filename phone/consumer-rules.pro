# Add consumer ProGuard rules here
# Keep public classes and methods that are part of the library API
-keep public class org.mjdev.phone.** {
    public *;
}
# Keep WebRTC classes
-keep class org.webrtc.** { *; }
# Keep Ktor client classes
-keep class io.ktor.** { *; }
# Keep serialization classes
-keep class kotlinx.serialization.** { *; }
# Keep DI framework classes
-keep class org.kodein.di.** { *; }