# Phone Module Publishing Guide

## Overview

This guide explains how to publish the Phone module to Maven and use it in other projects.

## Prerequisites

- Android Studio with Gradle
- Proper Maven credentials configured (for remote publishing)
- Local Maven repository setup (for local testing)

## Publishing Steps

### 1. Local Publishing (for testing)

Run the publish script:
```bash
./publish-phone-module.sh
```

Or manually:
```bash
./gradlew :phone:publishAllPublicationsToMavenLocal
```

### 2. Remote Publishing (to Maven Central/JitPack)

Configure your `local.properties` with Maven credentials:
```properties
maven.username=your_username
maven.password=your_password
```

Then run:
```bash
./gradlew :phone:publishAllPublicationsToMavenRepository
```

## Using the Published Module

In your project's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.mjdev:phone:1.0.0")
}
```

## Module Features

The Phone module provides:

- **Video Calling**: Full WebRTC video communication
- **Intercom**: Audio communication features
- **Camera Integration**: Camera handling and preview
- **Microphone Support**: Audio capture and processing
- **Network Communication**: Ktor-based networking
- **Dependency Injection**: Kodein DI integration
- **Modern UI**: Jetpack Compose components

## Required Permissions

The consuming app must declare these permissions in its `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<!-- Additional permissions as needed -->
```

## Activities Provided

- `org.mjdev.phone.activity.IntercomActivity` - Main intercom interface
- `org.mjdev.phone.activity.VideoCallActivity` - Video call functionality

## ProGuard Configuration

The module includes proper ProGuard rules. Consumers should add:

```proguard
# Keep phone module classes
-keep class org.mjdev.phone.** { *; }
# Keep WebRTC
-keep class org.webrtc.** { *; }
# Keep networking libraries
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }
```

## Versioning

Current version: `1.0.0`

Follow semantic versioning:
- MAJOR version for incompatible API changes
- MINOR version for backward-compatible functionality
- PATCH version for backward-compatible bug fixes

## Troubleshooting

### Common Issues

1. **ClassNotFoundException**: Ensure all dependencies are properly included
2. **Permission denied**: Check that all required permissions are declared
3. **ProGuard issues**: Verify ProGuard rules are correctly applied
4. **WebRTC initialization failed**: Check device compatibility and permissions

### Debugging

Enable logging in your app:
```kotlin
System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG")
```

## Contributing

1. Fork the repository
2. Create feature branch
3. Make changes
4. Test thoroughly
5. Submit pull request

## License

Apache License 2.0 - see LICENSE file for details
