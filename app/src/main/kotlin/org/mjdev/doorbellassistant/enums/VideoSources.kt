package org.mjdev.doorbellassistant.enums

enum class VideoSources(
    val path: String
) {
    Welcome("welcome.mp4"),
    Unavailable("unavailable.mp4"),
    Warning("warning.mp4"),
    RecordStarting("record.mp4"),
    RecordSent("sent.mp4"),
    Ringing("ringing.mp4"),
}