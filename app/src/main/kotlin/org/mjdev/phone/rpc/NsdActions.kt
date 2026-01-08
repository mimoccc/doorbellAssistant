package org.mjdev.phone.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.mjdev.phone.nsd.device.NsdDevice

object NsdActions {

    @Serializable
    @SerialName("NsdActionCall")
    open class NsdActionCall(
        open val caller: NsdDevice?,
        open val callee: NsdDevice?
    ) : NsdAction()

}