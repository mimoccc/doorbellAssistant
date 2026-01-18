package org.mjdev.doorbellassistant.agent.tts

enum class PiperModelType(
    val assetDir:String,
    val modelFileNameOnnx: String,
    val modelFileNameJson: String
) {
    CS(
        "cs",
        "cs.onnx",
        "cs.json"
    )
}