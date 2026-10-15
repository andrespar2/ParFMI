package com.andrespar2.parfmi.data.settings

import com.andrespar2.parfmi.data.model.ProviderService

/** Origen del catalogo de servicios activo. */
enum class CatalogSource {
    /** Lista incluida en la app con IDs reales del proveedor. */
    INCLUDED,

    /** Lista descargada desde el propio proveedor. */
    LIVE,
}

/** Configuracion efectiva de la aplicacion. */
data class AppSettings(
    val providerId: String,
    val serviceId: String,
    val apiKeyConfigured: Boolean,
    val services: List<ProviderService>,
    val catalogSource: CatalogSource,
) {
    val selectedService: ProviderService?
        get() = services.firstOrNull { it.id == serviceId }

    val selectedServiceName: String
        get() = selectedService?.name ?: serviceId
}
