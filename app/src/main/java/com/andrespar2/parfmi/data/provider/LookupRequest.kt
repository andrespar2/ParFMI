package com.andrespar2.parfmi.data.provider

import com.andrespar2.parfmi.core.DeviceIdentifier

/**
 * Peticion de consulta a un proveedor.
 *
 * [apiKey] se pasa en cada peticion y no se guarda en el proveedor: asi el
 * secreto no permanece en memoria mas tiempo del necesario.
 */
data class LookupRequest(
    val identifier: DeviceIdentifier,
    val serviceId: String,
    val serviceName: String?,
    val apiKey: String,
)
