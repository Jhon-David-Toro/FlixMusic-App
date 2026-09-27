package com.flix.music.core.model

enum class FlixError {
    NoConnection,
    ServerUnreachable,
    Timeout,
    Upstream,
    Unknown
}

class FlixException(
    val error: FlixError,
    cause: Throwable? = null,
) : Exception(error.name, cause)