package org.mjdev.doorbellassistant.enums

import org.mjdev.doorbellassistant.BuildConfig

enum class IntentAction(
    val action: String
) {
    MOTION_DETECTED(BuildConfig.APPLICATION_ID + ".MOTION_DETECTED")
}
