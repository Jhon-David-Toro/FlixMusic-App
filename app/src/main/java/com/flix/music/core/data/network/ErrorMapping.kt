package com.flix.music.core.data.network

import com.flix.music.core.model.FlixError
import com.flix.music.core.model.FlixException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

fun Throwable.toFlixException(): FlixException = FlixException(
    error = when (this) {
        is UnknownHostException, is NoRouteToHostException -> FlixError.NoConnection
        is ConnectException -> FlixError.ServerUnreachable
        is SocketTimeoutException ->
            if (message?.startsWith("failed to connect") == true) FlixError.ServerUnreachable
            else FlixError.Timeout
        is HttpException -> if (code() >= 500) FlixError.Upstream else FlixError.Unknown
        else -> FlixError.Unknown
    },
    cause = this,
)

suspend inline fun <T> apiCall(block: () -> T): T =
    try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: Exception) {
        throw exception.toFlixException()
    }