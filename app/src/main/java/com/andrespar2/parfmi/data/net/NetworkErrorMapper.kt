package com.andrespar2.parfmi.data.net

import com.andrespar2.parfmi.data.model.ProviderError
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Traduce fallos de red y codigos HTTP a las categorias que la interfaz sabe
 * explicar. Es una funcion pura y por tanto se puede probar sin dispositivo.
 */
object NetworkErrorMapper {

    fun fromException(throwable: Throwable): ProviderError = when (throwable) {
        is SocketTimeoutException -> ProviderError.TIMEOUT
        is InterruptedIOException -> ProviderError.TIMEOUT
        is UnknownHostException -> ProviderError.NO_NETWORK
        is ConnectException -> ProviderError.NO_NETWORK
        is NoRouteToHostException -> ProviderError.NO_NETWORK
        is SSLException -> ProviderError.UNKNOWN
        is IOException -> ProviderError.NO_NETWORK
        else -> ProviderError.UNKNOWN
    }

    fun fromStatusCode(code: Int): ProviderError = when {
        code == 401 || code == 403 -> ProviderError.UNAUTHORIZED
        code == 402 -> ProviderError.INSUFFICIENT_BALANCE
        code == 404 -> ProviderError.NOT_FOUND
        code == 408 -> ProviderError.TIMEOUT
        code == 429 -> ProviderError.RATE_LIMITED
        code in 500..599 -> ProviderError.SERVER_ERROR
        code in 400..499 -> ProviderError.REQUEST_REJECTED
        else -> ProviderError.UNKNOWN
    }
}
