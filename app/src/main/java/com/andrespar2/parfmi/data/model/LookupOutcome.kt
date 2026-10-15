package com.andrespar2.parfmi.data.model

/** Resultado de una consulta al proveedor. */
sealed interface LookupOutcome {

    data class Success(val report: FmiReport) : LookupOutcome

    /**
     * Fallo de la consulta.
     *
     * @param detail informacion tecnica ya enmascarada (nunca contiene el
     * identificador completo ni la API key) util para diagnostico.
     */
    data class Failure(
        val error: ProviderError,
        val detail: String? = null,
    ) : LookupOutcome
}
