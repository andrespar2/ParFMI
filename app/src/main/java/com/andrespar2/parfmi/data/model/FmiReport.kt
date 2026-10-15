package com.andrespar2.parfmi.data.model

import com.andrespar2.parfmi.core.DeviceIdentifier

/** Par etiqueta/valor adicional devuelto por el proveedor. */
data class LookupField(
    val label: String,
    val value: String,
)

/** Datos de identidad y descripcion del dispositivo. */
data class DeviceInfo(
    val imei: String? = null,
    val serialNumber: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val modelName: String? = null,
    val modelCode: String? = null,
    val productFamily: String? = null,
    val color: String? = null,
    val capacity: String? = null,
    val partNumber: String? = null,
) {
    /** Indica si el proveedor aporto algun dato de dispositivo. */
    val hasAnyValue: Boolean
        get() = listOf(
            imei, serialNumber, manufacturer, model, modelName, modelCode,
            productFamily, color, capacity, partNumber,
        ).any { !it.isNullOrBlank() }
}

/** Indicadores de seguridad y bloqueo. */
data class SecurityInfo(
    val fmi: FmiStatus = FmiStatus.UNKNOWN,
    val activationLock: ActivationLockStatus = ActivationLockStatus.UNKNOWN,
    val activationState: ActivationState = ActivationState.UNKNOWN,
    val icloudState: IcloudState = IcloudState.UNKNOWN,
    val blacklist: BlacklistStatus = BlacklistStatus.UNKNOWN,
    val mdm: MdmStatus = MdmStatus.UNKNOWN,
    val simLock: SimLockStatus = SimLockStatus.UNKNOWN,
) {
    val hasAnyKnownValue: Boolean
        get() = fmi != FmiStatus.UNKNOWN ||
            activationLock != ActivationLockStatus.UNKNOWN ||
            activationState != ActivationState.UNKNOWN ||
            icloudState != IcloudState.UNKNOWN ||
            blacklist != BlacklistStatus.UNKNOWN ||
            mdm != MdmStatus.UNKNOWN ||
            simLock != SimLockStatus.UNKNOWN
}

/** Cobertura, garantia y fechas relacionadas. */
data class CoverageInfo(
    val status: CoverageStatus = CoverageStatus.UNKNOWN,
    val statusText: String? = null,
    val purchaseDate: String? = null,
    val activationDate: String? = null,
    val technicalSupport: String? = null,
    val repairsCoverage: String? = null,
    val appleCare: String? = null,
) {
    val hasAnyValue: Boolean
        get() = status != CoverageStatus.UNKNOWN || listOf(
            statusText, purchaseDate, activationDate, technicalSupport,
            repairsCoverage, appleCare,
        ).any { !it.isNullOrBlank() }
}

/** Red y operador. */
data class NetworkInfo(
    val carrier: String? = null,
    val country: String? = null,
    val region: String? = null,
) {
    val hasAnyValue: Boolean
        get() = listOf(carrier, country, region).any { !it.isNullOrBlank() }
}

/**
 * Resultado completo y trazable de una consulta.
 *
 * Todos los campos son opcionales: si el proveedor no los devuelve quedan a
 * `null` y la interfaz los muestra como "No disponible".
 */
data class FmiReport(
    val identifier: DeviceIdentifier,
    val providerId: String,
    val providerName: String,
    val serviceId: String,
    val serviceName: String? = null,
    val queriedAtMillis: Long,
    val device: DeviceInfo = DeviceInfo(),
    val security: SecurityInfo = SecurityInfo(),
    val coverage: CoverageInfo = CoverageInfo(),
    val network: NetworkInfo = NetworkInfo(),
    val extraFields: List<LookupField> = emptyList(),
    val balance: String? = null,
    val rawResponse: String? = null,
) {
    /** Etiqueta del tipo de identificador consultado. */
    val identifierLabel: String
        get() = when (identifier.type) {
            com.andrespar2.parfmi.core.IdentifierType.IMEI -> "IMEI"
            com.andrespar2.parfmi.core.IdentifierType.SERIAL_NUMBER -> "Numero de serie"
            com.andrespar2.parfmi.core.IdentifierType.UNKNOWN -> "Identificador"
        }
}
