package com.andrespar2.parfmi.data.net

import com.andrespar2.parfmi.data.model.ProviderError

/** Resultado de una peticion HTTP. */
sealed interface HttpResult {
    data class Response(val code: Int, val body: String) : HttpResult

    /**
     * Fallo de red. [detail] es informacion tecnica sin datos sensibles
     * (por ejemplo el tipo de excepcion), nunca la URL completa.
     */
    data class NetworkFailure(val error: ProviderError, val detail: String?) : HttpResult
}

/** Abstraccion minima de red para poder sustituirla en pruebas. */
interface HttpEngine {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResult
}
