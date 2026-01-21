/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.enums

import org.mjdev.phone.helpers.json.Serializable

@Serializable
open class VideoSources(
    val path: String
) {
    @Serializable
    object Welcome : VideoSources("video/welcome.mp4")

    @Serializable
    object Unavailable : VideoSources("video/unavailable.mp4")

    @Serializable
    object Warning : VideoSources("video/warning.mp4")

    @Serializable
    object RecordStarting : VideoSources("video/record.mp4")

    @Serializable
    object RecordSent : VideoSources("video/sent.mp4")

    @Serializable
    object Ringing : VideoSources("video/ringing.mp4")
}