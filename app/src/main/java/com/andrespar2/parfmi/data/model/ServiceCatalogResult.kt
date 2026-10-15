package com.andrespar2.parfmi.data.model

/** Resultado de pedir el catalogo de servicios al proveedor. */
sealed interface ServiceCatalogResult {
    data class Success(val services: List<ProviderService>) : ServiceCatalogResult

    data class Failure(
        val error: ProviderError,
        val detail: String? = null,
    ) : ServiceCatalogResult
}
