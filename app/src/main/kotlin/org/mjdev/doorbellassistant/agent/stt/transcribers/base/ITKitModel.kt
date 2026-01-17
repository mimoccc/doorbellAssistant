package org.mjdev.doorbellassistant.agent.stt.transcribers.base

interface ITKitModel {
    val modelName: String
    val url: String
    val frequency: Int
    val channels: Int
    val assetsFolder : String
}