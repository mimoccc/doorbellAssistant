/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.agent.stt.transcribers.vosk

import org.mjdev.doorbellassistant.agent.stt.transcribers.base.ITKitModel
import org.mjdev.phone.helpers.json.Serializable

@Serializable
@Suppress("ClassName", "unused")
open class VoskModelType(
    override val modelName: String,
    override val url: String,
    override val frequency: Int = 16000,
    override val channels: Int = 1,
    override val assetsFolder : String = "vosk",
    val lang: VoskLanguage,
    val size: VoskSize,
) : ITKitModel {

    @Serializable
    object CS_SMALL: VoskModelType(
        modelName = "Small CS",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-cs-0.4-rhasspy.zip",
        lang = VoskLanguage.CS,
        size = VoskSize.SMALL,
    )

    @Serializable
    object EN_SMALL : VoskModelType(
        modelName = "Small EN",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
        lang = VoskLanguage.EN,
        size = VoskSize.SMALL,
    )

}
