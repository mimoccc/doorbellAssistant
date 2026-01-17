package org.mjdev.doorbellassistant.agent.stt.transcribers.base

interface ITKit {
    @Suppress("UPPER_BOUND_VIOLATED_IN_TYPE_OPERATOR_OR_PARAMETER_BOUNDS_WARNING")
    fun setModel(modelType: ITKitModel)
    fun setCallback(callback: suspend (KitResult) -> Unit)
    fun init()
    fun release()
    fun transcribe(data: ByteArray)
}