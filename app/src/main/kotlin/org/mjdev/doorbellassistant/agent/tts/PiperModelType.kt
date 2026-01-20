package org.mjdev.doorbellassistant.agent.tts

@Suppress("unused")
enum class PiperModelType(
    val assetDir: String,
    val language: String,
    val modelFileNameOnnx: String,
    val modelFileNameJson: String,
    val sampleRate: Int,
    val tuning: PiperVoiceTuning,
) {
    CS(
        assetDir = "piper",
        language = "cs",
        modelFileNameOnnx = "cs.onnx",
        modelFileNameJson = "cs.json",
        sampleRate = 22050,
        tuning = PiperVoiceTuning.YoungCool,
    ),
    EN(
        assetDir = "piper",
        language = "en",
        modelFileNameOnnx = "en.onnx",
        modelFileNameJson = "en.json",
        sampleRate = 16000,
        tuning = PiperVoiceTuning.YoungCool,
    );
}
