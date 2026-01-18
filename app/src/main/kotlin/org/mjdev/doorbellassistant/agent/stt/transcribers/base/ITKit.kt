package org.mjdev.doorbellassistant.agent.stt.transcribers.base

interface ITKit {
    fun setModel(modelType: ITKitModel)
    fun init()
    fun release()
    fun transcribe(data: ByteArray)
    fun subscribe(
        onError: (Throwable) -> Unit = {},
        onEvent: (ITKitResult) -> Unit,
    )
}