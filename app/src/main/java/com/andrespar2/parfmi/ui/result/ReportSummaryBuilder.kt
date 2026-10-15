package com.andrespar2.parfmi.ui.result

import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.core.TimestampFormatter
import com.andrespar2.parfmi.data.model.ActivationLockStatus
import com.andrespar2.parfmi.data.model.ActivationState
import com.andrespar2.parfmi.data.model.BlacklistStatus
import com.andrespar2.parfmi.data.model.CoverageStatus
import com.andrespar2.parfmi.data.model.FmiReport
import com.andrespar2.parfmi.data.model.FmiStatus
import com.andrespar2.parfmi.data.model.IcloudState
import com.andrespar2.parfmi.data.model.MdmStatus
import com.andrespar2.parfmi.data.model.SimLockStatus

/**
 * Construye el resumen en texto plano que se copia al portapapeles o se
 * comparte desde la pantalla de resultados.
 *
 * Es codigo puro (sin APIs de Android) para poder probarlo en la JVM. Nunca
 * incluye la respuesta original del proveedor ni la API key: solo los campos
 * ya interpretados del informe.
 *
 * Los estados desconocidos se escriben como "Desconocido", nunca como "OFF"
 * ni "desbloqueado", para no dar una falsa sensacion de seguridad.
 */
object ReportSummaryBuilder {

    private const val NOT_AVAILABLE = "No disponible"
    private const val UNKNOWN = "Desconocido"

    fun build(report: FmiReport): String = buildString {
        appendLine("ParFMI - Resultado de la consulta")
        appendLine("Generado con ParFMI por andrespar2 - https://github.com/andrespar2")
        appendLine()

        appendLine("## Datos de la consulta")
        row(
            "Identificador (${identifierTypeLabel(report)})",
            report.identifier.raw,
        )
        row("Fecha y hora", TimestampFormatter.format(report.queriedAtMillis))
        row("Proveedor", report.providerName)
        row("Servicio", report.serviceName ?: report.serviceId)
        report.balance?.let { row("Saldo de la cuenta", it) }
        appendLine()

        appendLine("## Dispositivo")
        row("IMEI", report.device.imei)
        row("Numero de serie", report.device.serialNumber)
        row("Fabricante", report.device.manufacturer)
        row("Modelo", report.device.model)
        row("Nombre del modelo", report.device.modelName)
        row("Identificador del modelo", report.device.modelCode)
        row("Familia de producto", report.device.productFamily)
        row("Color", report.device.color)
        row("Capacidad", report.device.capacity)
        row("Numero de pieza (MPN)", report.device.partNumber)
        appendLine()

        appendLine("## Find My iPhone y bloqueos")
        row("Estado FMI", fmiLabel(report.security.fmi))
        row("Activation Lock", activationLockLabel(report.security.activationLock))
        row("Estado de activacion", activationStateLabel(report.security.activationState))
        row("Estado de iCloud", icloudLabel(report.security.icloudState))
        row("Lista de bloqueo", blacklistLabel(report.security.blacklist))
        row("MDM", mdmLabel(report.security.mdm))
        row("Bloqueo de operador", simLockLabel(report.security.simLock))
        appendLine()

        appendLine("## Cobertura y garantia")
        row("Cobertura", coverageLabel(report.coverage.status))
        report.coverage.statusText?.let { row("Detalle de cobertura", it) }
        report.coverage.purchaseDate?.let { row("Fecha de compra o activacion", it) }
        report.coverage.activationDate?.let { row("Fecha de activacion", it) }
        report.coverage.technicalSupport?.let { row("Soporte tecnico", it) }
        report.coverage.appleCare?.let { row("AppleCare", it) }
        appendLine()

        if (report.network.hasAnyValue) {
            appendLine("## Red y operador")
            row("Operador", report.network.carrier)
            row("Pais", report.network.country)
            row("Region", report.network.region)
            appendLine()
        }

        if (report.extraFields.isNotEmpty()) {
            appendLine("## Informacion adicional del proveedor")
            report.extraFields.forEach { row(it.label, it.value) }
            appendLine()
        }

        append("Los estados \"$UNKNOWN\" significan que el proveedor no devolvio ese dato.")
    }

    private fun StringBuilder.row(label: String, value: String?) {
        appendLine("- $label: ${value?.takeIf { it.isNotBlank() } ?: NOT_AVAILABLE}")
    }

    private fun identifierTypeLabel(report: FmiReport): String = when (report.identifier.type) {
        IdentifierType.IMEI -> "IMEI"
        IdentifierType.SERIAL_NUMBER -> "Numero de serie"
        IdentifierType.UNKNOWN -> "Identificador"
    }

    private fun fmiLabel(status: FmiStatus): String = when (status) {
        FmiStatus.ON -> "ON"
        FmiStatus.OFF -> "OFF"
        FmiStatus.UNKNOWN -> UNKNOWN
    }

    private fun activationLockLabel(status: ActivationLockStatus): String = when (status) {
        ActivationLockStatus.LOCKED -> "Bloqueado"
        ActivationLockStatus.UNLOCKED -> "Desbloqueado"
        ActivationLockStatus.UNKNOWN -> UNKNOWN
    }

    private fun activationStateLabel(state: ActivationState): String = when (state) {
        ActivationState.ACTIVATED -> "Activado"
        ActivationState.NOT_ACTIVATED -> "No activado"
        ActivationState.UNKNOWN -> UNKNOWN
    }

    private fun icloudLabel(state: IcloudState): String = when (state) {
        IcloudState.CLEAN -> "Sin incidencias"
        IcloudState.LOST -> "En modo perdido"
        IcloudState.ERASED -> "Borrado"
        IcloudState.UNKNOWN -> UNKNOWN
    }

    private fun blacklistLabel(status: BlacklistStatus): String = when (status) {
        BlacklistStatus.CLEAN -> "Sin bloqueo"
        BlacklistStatus.BLACKLISTED -> "En lista de bloqueo"
        BlacklistStatus.UNKNOWN -> UNKNOWN
    }

    private fun mdmLabel(status: MdmStatus): String = when (status) {
        MdmStatus.ENROLLED -> "Inscrito en MDM"
        MdmStatus.NOT_ENROLLED -> "Sin MDM"
        MdmStatus.UNKNOWN -> UNKNOWN
    }

    private fun simLockLabel(status: SimLockStatus): String = when (status) {
        SimLockStatus.LOCKED -> "Bloqueado a operador"
        SimLockStatus.UNLOCKED -> "Libre de operador"
        SimLockStatus.UNKNOWN -> UNKNOWN
    }

    private fun coverageLabel(status: CoverageStatus): String = when (status) {
        CoverageStatus.IN_WARRANTY -> "En garantia"
        CoverageStatus.OUT_OF_WARRANTY -> "Fuera de garantia"
        CoverageStatus.UNKNOWN -> UNKNOWN
    }
}
