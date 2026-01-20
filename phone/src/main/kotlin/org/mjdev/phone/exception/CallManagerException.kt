package org.mjdev.phone.exception

class CallManagerException(
    message: String = "Unspecified exception.",
    cause: Throwable? = null
) : RuntimeException(message, cause)
