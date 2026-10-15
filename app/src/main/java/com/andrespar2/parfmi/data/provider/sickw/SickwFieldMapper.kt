package com.andrespar2.parfmi.data.provider.sickw

import com.andrespar2.parfmi.core.DeviceIdentifier
import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.core.TextNormalizer
import com.andrespar2.parfmi.data.model.ActivationLockStatus
import com.andrespar2.parfmi.data.model.ActivationState
import com.andrespar2.parfmi.data.model.BlacklistStatus
import com.andrespar2.parfmi.data.model.CoverageInfo
import com.andrespar2.parfmi.data.model.CoverageStatus
import com.andrespar2.parfmi.data.model.DeviceInfo
import com.andrespar2.parfmi.data.model.FmiReport
import com.andrespar2.parfmi.data.model.FmiStatus
import com.andrespar2.parfmi.data.model.IcloudState
import com.andrespar2.parfmi.data.model.LookupField
import com.andrespar2.parfmi.data.model.MdmStatus
import com.andrespar2.parfmi.data.model.NetworkInfo
import com.andrespar2.parfmi.data.model.SecurityInfo
import com.andrespar2.parfmi.data.model.SimLockStatus
import java.util.Locale

/** Campo del informe al que se asigna una etiqueta devuelta por el proveedor. */
internal enum class FieldTarget {
    ACTIVATION_LOCK,
    ACTIVATION_DATE,
    SIM_LOCK,
    MDM,
    BLACKLIST,
    FMI,
    ICLOUD,
    ACTIVATION_STATE,
    TECHNICAL_SUPPORT,
    REPAIRS_COVERAGE,
    APPLE_CARE,
    COVERAGE,
    PURCHASE_DATE,
    PART_NUMBER,
    MODEL_CODE,
    MODEL_NAME,
    MODEL,
    PRODUCT_FAMILY,
    SERIAL,
    IMEI,
    MANUFACTURER,
    COLOR,
    CAPACITY,
    CARRIER,
    COUNTRY,
    REGION,
    EXTRA,
}

/**
 * Convierte la respuesta de Sickw en el modelo de dominio de ParFMI.
 *
 * Sickw devuelve un conjunto de etiquetas distinto para cada servicio, por lo
 * que el mapeo se hace por palabras clave en lugar de por nombres exactos.
 * Cuando una etiqueta no coincide con ningun campo conocido se conserva en
 * "informacion adicional" en lugar de descartarse.
 *
 * Reglas de estado:
 *  - Solo se marca ON/OFF (o bloqueado/desbloqueado) cuando el proveedor lo
 *    indica de forma explicita.
 *  - Si el proveedor informa del estado de iCloud o de Activation Lock, se
 *    deduce el estado FMI solo en ese caso, porque en Apple ambos dependen del
 *    mismo mecanismo. En cualquier otro caso el estado queda como desconocido.
 */
class SickwFieldMapper {

    fun toReport(
        payload: SickwPayload,
        identifier: DeviceIdentifier,
        serviceId: String,
        serviceName: String?,
        queriedAtMillis: Long,
        rawResponse: String?,
    ): FmiReport {
        var imei: String? = null
        var serial: String? = null
        var manufacturer: String? = null
        var model: String? = null
        var modelName: String? = null
        var modelCode: String? = null
        var productFamily: String? = null
        var color: String? = null
        var capacity: String? = null
        var partNumber: String? = null

        var fmi = FmiStatus.UNKNOWN
        var icloudState = IcloudState.UNKNOWN
        var activationLock = ActivationLockStatus.UNKNOWN
        var activationState = ActivationState.UNKNOWN
        var blacklist = BlacklistStatus.UNKNOWN
        var mdm = MdmStatus.UNKNOWN
        var simLock = SimLockStatus.UNKNOWN

        var coverageStatus = CoverageStatus.UNKNOWN
        var coverageText: String? = null
        var purchaseDate: String? = null
        var activationDate: String? = null
        var technicalSupport: String? = null
        var repairsCoverage: String? = null
        var appleCare: String? = null

        var carrier: String? = null
        var country: String? = null
        var region: String? = null

        val extraFields = mutableListOf<LookupField>()

        payload.fields.forEach { (rawLabel, rawValue) ->
            val value = TextNormalizer.displayOrNull(rawValue)
            when (classifyLabel(rawLabel)) {
                FieldTarget.ACTIVATION_LOCK -> {
                    activationLock = ActivationLockStatus.fromRaw(rawValue)
                }

                FieldTarget.ACTIVATION_DATE -> activationDate = activationDate ?: value

                FieldTarget.SIM_LOCK -> simLock = SimLockStatus.fromRaw(rawValue)

                FieldTarget.MDM -> mdm = MdmStatus.fromRaw(rawValue)

                FieldTarget.BLACKLIST -> blacklist = BlacklistStatus.fromRaw(rawValue)

                FieldTarget.FMI -> {
                    fmi = FmiStatus.fromRaw(rawValue)
                }

                FieldTarget.ICLOUD -> {
                    val asFmi = FmiStatus.fromRaw(rawValue)
                    if (asFmi != FmiStatus.UNKNOWN) {
                        fmi = asFmi
                    } else {
                        icloudState = IcloudState.fromRaw(rawValue)
                    }
                }

                FieldTarget.ACTIVATION_STATE -> {
                    activationState = ActivationState.fromRaw(rawValue)
                }

                FieldTarget.TECHNICAL_SUPPORT -> technicalSupport = technicalSupport ?: value

                FieldTarget.REPAIRS_COVERAGE -> repairsCoverage = repairsCoverage ?: value

                FieldTarget.APPLE_CARE -> appleCare = appleCare ?: value

                FieldTarget.COVERAGE -> {
                    coverageStatus = CoverageStatus.fromRaw(rawValue)
                    coverageText = coverageText ?: value
                }

                FieldTarget.PURCHASE_DATE -> purchaseDate = purchaseDate ?: value

                FieldTarget.PART_NUMBER -> partNumber = partNumber ?: value

                FieldTarget.MODEL_CODE -> modelCode = modelCode ?: value

                FieldTarget.MODEL_NAME -> modelName = modelName ?: value

                FieldTarget.MODEL -> model = model ?: value

                FieldTarget.PRODUCT_FAMILY -> productFamily = productFamily ?: value

                FieldTarget.SERIAL -> serial = serial ?: value

                FieldTarget.IMEI -> imei = imei ?: value

                FieldTarget.MANUFACTURER -> manufacturer = manufacturer ?: value

                FieldTarget.COLOR -> color = color ?: value

                FieldTarget.CAPACITY -> capacity = capacity ?: value

                FieldTarget.CARRIER -> carrier = carrier ?: value

                FieldTarget.COUNTRY -> country = country ?: value

                FieldTarget.REGION -> region = region ?: value

                FieldTarget.EXTRA -> if (value != null) {
                    extraFields += LookupField(label = rawLabel, value = value)
                }
            }
        }

        payload.textPayload?.takeIf { it.isNotBlank() }?.let { text ->
            extraFields += LookupField(label = "Resultado del proveedor", value = text)
        }

        // Deduccion conservadora: Apple liga Activation Lock e iCloud a Find My.
        if (fmi == FmiStatus.UNKNOWN) {
            fmi = when {
                activationLock == ActivationLockStatus.LOCKED -> FmiStatus.ON
                activationLock == ActivationLockStatus.UNLOCKED -> FmiStatus.OFF
                icloudState == IcloudState.LOST || icloudState == IcloudState.ERASED -> FmiStatus.ON
                icloudState == IcloudState.CLEAN -> FmiStatus.OFF
                else -> FmiStatus.UNKNOWN
            }
        }

        // Si el proveedor informa del estado de activacion pero no del
        // identificador consultado, se muestra el que introdujo el usuario.
        if (imei == null && identifier.type == IdentifierType.IMEI) imei = identifier.normalized
        if (serial == null && identifier.type == IdentifierType.SERIAL_NUMBER) {
            serial = identifier.normalized
        }

        return FmiReport(
            identifier = identifier,
            providerId = SickwServiceCatalog.PROVIDER_ID,
            providerName = SickwServiceCatalog.PROVIDER_NAME,
            serviceId = serviceId,
            serviceName = serviceName,
            queriedAtMillis = queriedAtMillis,
            device = DeviceInfo(
                imei = imei,
                serialNumber = serial,
                manufacturer = manufacturer,
                model = model,
                modelName = modelName,
                modelCode = modelCode,
                productFamily = productFamily,
                color = color,
                capacity = capacity,
                partNumber = partNumber,
            ),
            security = SecurityInfo(
                fmi = fmi,
                activationLock = activationLock,
                activationState = activationState,
                icloudState = icloudState,
                blacklist = blacklist,
                mdm = mdm,
                simLock = simLock,
            ),
            coverage = CoverageInfo(
                status = coverageStatus,
                statusText = coverageText,
                purchaseDate = purchaseDate,
                activationDate = activationDate,
                technicalSupport = technicalSupport,
                repairsCoverage = repairsCoverage,
                appleCare = appleCare,
            ),
            network = NetworkInfo(
                carrier = carrier,
                country = country,
                region = region,
            ),
            extraFields = extraFields,
            balance = payload.balance,
            rawResponse = rawResponse,
        )
    }

    /**
     * Asigna una etiqueta a un campo del informe.
     *
     * El orden de las comprobaciones importa: las etiquetas mas especificas se
     * evaluan antes que las genericas ("Activation Lock" antes que
     * "Activation", "Carrier Lock" antes que "Carrier").
     */
    internal fun classifyLabel(label: String): FieldTarget {
        val text = TextNormalizer.normalize(label).lowercase(Locale.ROOT)
        if (text.isEmpty()) return FieldTarget.EXTRA

        return when {
            text.contains("activation lock") -> FieldTarget.ACTIVATION_LOCK

            text.contains("activation date") || text.contains("activated date") ||
                text.contains("date of activation") -> FieldTarget.ACTIVATION_DATE

            text.contains("simlock") || text.contains("sim lock") ||
                text.contains("carrier lock") || text.contains("network lock") ||
                text.contains("lock status") -> FieldTarget.SIM_LOCK

            text.contains("mdm") -> FieldTarget.MDM

            text.contains("blacklist") || text.contains("black list") ||
                text.contains("stolen") -> FieldTarget.BLACKLIST

            text.contains("fmi") -> FieldTarget.FMI

            text.contains("icloud") -> FieldTarget.ICLOUD

            text.contains("activation") || text.contains("activated") ->
                FieldTarget.ACTIVATION_STATE

            text.contains("technical support") -> FieldTarget.TECHNICAL_SUPPORT

            text.contains("repair") -> FieldTarget.REPAIRS_COVERAGE

            text.contains("apple care") || text.contains("applecare") -> FieldTarget.APPLE_CARE

            text.contains("warranty") || text.contains("coverage") -> FieldTarget.COVERAGE

            text.contains("purchase date") || text.contains("sold date") ||
                text.contains("estimated purchase") -> FieldTarget.PURCHASE_DATE

            text.contains("part number") || text.contains("mpn") -> FieldTarget.PART_NUMBER

            text.contains("model code") || text.contains("model number") ||
                text.contains("model no") || text.contains("a number") -> FieldTarget.MODEL_CODE

            text.contains("model name") || text.contains("model description") ->
                FieldTarget.MODEL_NAME

            text.contains("model") || text.contains("device") -> FieldTarget.MODEL

            text.contains("product family") || text.contains("family") -> FieldTarget.PRODUCT_FAMILY

            text.contains("serial") -> FieldTarget.SERIAL

            text.contains("imei") -> FieldTarget.IMEI

            text.contains("manufacturer") || text.contains("brand") || text.contains("maker") ||
                text.contains("vendor") -> FieldTarget.MANUFACTURER

            text.contains("color") || text.contains("colour") -> FieldTarget.COLOR

            text.contains("capacity") || text.contains("storage") ||
                text.contains("memory") -> FieldTarget.CAPACITY

            text.contains("carrier") || text.contains("operator") || text.contains("network") ->
                FieldTarget.CARRIER

            text.contains("country") -> FieldTarget.COUNTRY

            text.contains("region") -> FieldTarget.REGION

            else -> FieldTarget.EXTRA
        }
    }
}
