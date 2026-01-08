package org.mjdev.phone.exception

class NsdException(
    message: String = "Unspecified exception.",
    cause: Throwable? = null
) : RuntimeException(message, cause)
