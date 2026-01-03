package org.mjdev.doorbellassistant.data

data class DnsInfo(
    val domain: String,
    val uid: Int,
    val destinationIp: String
)