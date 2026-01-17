package org.mjdev.doorbellassistant.agent.stt.transcribers.vosk

import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel

@Suppress("ClassName", "unused")
sealed class VoskModelType(
    override val modelName: String,
    override val url: String,
    override val frequency: Int = 16000,
    override val channels: Int = 1,
    override val assetsFolder : String = "vosk",
    val lang: VoskLanguage,
    val size: VoskSize,
) : ITKitModel {
    object CS_SMALL: VoskModelType(
        modelName = "Small CS",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-cs-0.4-rhasspy.zip",
        lang = VoskLanguage.CS,
        size = VoskSize.SMALL,
    )
    object EN_SMALL : VoskModelType(
        modelName = "Small EN",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
        lang = VoskLanguage.EN,
        size = VoskSize.SMALL,
    )
}
