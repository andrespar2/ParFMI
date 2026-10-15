package com.andrespar2.parfmi.data.model

/**
 * Servicio concreto ofrecido por un proveedor.
 *
 * @param id identificador que espera la API (por ejemplo "3" en Sickw).
 * @param name nombre publicado por el proveedor.
 * @param price precio publicado, en la moneda que indique el proveedor.
 * @param defaultService indica si es el servicio preseleccionado en la app.
 */
data class ProviderService(
    val id: String,
    val name: String,
    val price: String? = null,
    val defaultService: Boolean = false,
) {
    /** Texto listo para mostrar en un selector. */
    fun displayLabel(): String = if (price.isNullOrBlank()) name else "$name ($price)"
}
