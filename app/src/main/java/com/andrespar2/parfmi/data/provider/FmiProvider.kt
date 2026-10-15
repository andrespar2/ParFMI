package com.andrespar2.parfmi.data.provider

import com.andrespar2.parfmi.data.model.LookupOutcome
import com.andrespar2.parfmi.data.model.ServiceCatalogResult

/**
 * Contrato de un proveedor de consultas FMI.
 *
 * La app implementa hoy el proveedor de Sickw.com. Cualquier proveedor futuro
 * (por ejemplo GSX a traves de un AASP autorizado) solo necesita implementar
 * esta interfaz y registrarse en el contenedor de dependencias.
 */
interface FmiProvider {

    /** Identificador interno del proveedor. */
    val id: String

    /** Nombre que se muestra al usuario. */
    val displayName: String

    /** Ejecuta una consulta real y devuelve un informe o un error tipificado. */
    suspend fun lookup(request: LookupRequest): LookupOutcome

    /** Intenta descargar el catalogo de servicios del proveedor. */
    suspend fun fetchServices(apiKey: String): ServiceCatalogResult
}
