package org.mjdev.doorbellassistant.enums

enum class VideoSources(
    val path: String
) {
    Welcome("video/welcome.mp4"),
    Unavailable("video/unavailable.mp4"),
    Warning("video/warning.mp4"),
    RecordStarting("video/record.mp4"),
    RecordSent("video/sent.mp4"),
    Ringing("video/ringing.mp4"),
}