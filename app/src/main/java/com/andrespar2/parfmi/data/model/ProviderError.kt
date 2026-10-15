package com.andrespar2.parfmi.data.model

/**
 * Categorias de error que la interfaz sabe explicar al usuario.
 *
 * Ninguna categoria incluye el identificador consultado ni la API key: los
 * detalles tecnicos se guardan enmascarados en [LookupOutcome.Failure.detail].
 */
enum class ProviderError {
    /** No hay conexion a Internet. */
    NO_NETWORK,

    /** El proveedor no respondio a tiempo. */
    TIMEOUT,

    /** API key ausente, caducada o rechazada. */
    UNAUTHORIZED,

    /** La cuenta del proveedor no tiene saldo para el servicio elegido. */
    INSUFFICIENT_BALANCE,

    /** El proveedor no encontro informacion para ese identificador. */
    NOT_FOUND,

    /** Se superaron los limites de consultas. */
    RATE_LIMITED,

    /** Error interno (5xx) del proveedor. */
    SERVER_ERROR,

    /** La respuesta no tiene el formato esperado. */
    PARSE_ERROR,

    /** No hay proveedor configurado en Ajustes. */
    NO_PROVIDER_CONFIGURED,

    /** El proveedor rechazo la peticion por otro motivo. */
    REQUEST_REJECTED,

    /** Error no clasificado. */
    UNKNOWN,
}
