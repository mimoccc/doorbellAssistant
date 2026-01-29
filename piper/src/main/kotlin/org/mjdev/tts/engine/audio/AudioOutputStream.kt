/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.tts.engine.audio;

import android.media.AudioAttributes;

enum class AudioOutputStream {
    SONIFICATION,
    MEDIA,
    ALARM,
    NOTIFICATION,
    VOICE_COMMUNICATION,
    ASSISTANT;

    internal fun toUsage(): Int = when (this) {
        SONIFICATION -> AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
        MEDIA -> AudioAttributes.USAGE_MEDIA
        ALARM -> AudioAttributes.USAGE_ALARM
        NOTIFICATION -> AudioAttributes.USAGE_NOTIFICATION
        VOICE_COMMUNICATION -> AudioAttributes.USAGE_VOICE_COMMUNICATION
        ASSISTANT -> AudioAttributes.USAGE_ASSISTANT
    }
}