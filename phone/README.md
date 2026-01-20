# Phone Module

Android library for phone call and video communication features.

## Features

- Video calling capabilities
- Intercom functionality  
- WebRTC integration
- Camera and microphone handling
- Network communication

## Setup

Add to your `build.gradle`:

```kotlin
implementation("org.mjdev:phone:1.0.0")
```

## Permissions Required

The library requires the following permissions:
- CAMERA
- RECORD_AUDIO
- INTERNET
- WAKE_LOCK
- FOREGROUND_SERVICE
- And various other communication-related permissions

## Components

### Activities
- `IntercomActivity` - Main intercom interface
- `VideoCallActivity` - Video call functionality

### Services
Various foreground services for camera, microphone, and media playback

## Usage

Initialize the phone module in your application and use the provided activities for communication features.

## License

Apache License 2.0
