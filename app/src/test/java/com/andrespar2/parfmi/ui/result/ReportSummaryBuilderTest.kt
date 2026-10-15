package com.andrespar2.parfmi.ui.result

import com.andrespar2.parfmi.core.DeviceIdentifier
import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.core.TimestampFormatter
import com.andrespar2.parfmi.data.model.ActivationLockStatus
import com.andrespar2.parfmi.data.model.BlacklistStatus
import com.andrespar2.parfmi.data.model.CoverageInfo
import com.andrespar2.parfmi.data.model.CoverageStatus
import com.andrespar2.parfmi.data.model.DeviceInfo
import com.andrespar2.parfmi.data.model.FmiReport
import com.andrespar2.parfmi.data.model.FmiStatus
import com.andrespar2.parfmi.data.model.LookupField
import com.andrespar2.parfmi.data.model.NetworkInfo
import com.andrespar2.parfmi.data.model.SecurityInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportSummaryBuilderTest {

    private val imeiIdentifier = DeviceIdentifier(
        raw = "356938035643809",
        normalized = "356938035643809",
        type = IdentifierType.IMEI,
    )

    private val queriedAt = 1_700_000_000_000L

    private fun report(
        device: DeviceInfo = DeviceInfo(),
        security: SecurityInfo = SecurityInfo(),
        coverage: CoverageInfo = CoverageInfo(),
        network: NetworkInfo = NetworkInfo(),
        extraFields: List<LookupField> = emptyList(),
        rawResponse: String? = null,
    ) = FmiReport(
        identifier = imeiIdentifier,
        providerId = "sickw",
        providerName = "Sickw.com",
        serviceId = "72",
        serviceName = "Apple: operador + MDM",
        queriedAtMillis = queriedAt,
        device = device,
        security = security,
        coverage = coverage,
        network = network,
        extraFields = extraFields,
        rawResponse = rawResponse,
    )

    @Test
    fun `includes identifier type and value`() {
        val text = ReportSummaryBuilder.build(report())
        assertTrue(text.contains("Identificador (IMEI): 356938035643809"))
    }

    @Test
    fun `includes query timestamp provider and service`() {
        val text = ReportSummaryBuilder.build(report())
        assertTrue(text.contains("Fecha y hora: ${TimestampFormatter.format(queriedAt)}"))
        assertTrue(text.contains("Proveedor: Sickw.com"))
        assertTrue(text.contains("Servicio: Apple: operador + MDM"))
    }

    @Test
    fun `maps FMI ON OFF and unknown correctly`() {
        assertTrue(
            ReportSummaryBuilder.build(report(security = SecurityInfo(fmi = FmiStatus.ON)))
                .contains("Estado FMI: ON"),
        )
        assertTrue(
            ReportSummaryBuilder.build(report(security = SecurityInfo(fmi = FmiStatus.OFF)))
                .contains("Estado FMI: OFF"),
        )
        assertTrue(
            ReportSummaryBuilder.build(report(security = SecurityInfo(fmi = FmiStatus.UNKNOWN)))
                .contains("Estado FMI: Desconocido"),
        )
    }

    @Test
    fun `never shows unknown as off`() {
        val text = ReportSummaryBuilder.build(report())
        assertTrue(text.contains("Estado FMI: Desconocido"))
        assertFalse(text.contains("Estado FMI: OFF"))
        assertTrue(text.contains("Activation Lock: Desconocido"))
        assertFalse(text.contains("Activation Lock: Desbloqueado"))
    }

    @Test
    fun `maps activation lock blacklist and coverage`() {
        val text = ReportSummaryBuilder.build(
            report(
                security = SecurityInfo(
                    activationLock = ActivationLockStatus.LOCKED,
                    blacklist = BlacklistStatus.CLEAN,
                ),
                coverage = CoverageInfo(status = CoverageStatus.IN_WARRANTY),
            ),
        )
        assertTrue(text.contains("Activation Lock: Bloqueado"))
        assertTrue(text.contains("Lista de bloqueo: Sin bloqueo"))
        assertTrue(text.contains("Cobertura: En garantia"))
    }

    @Test
    fun `shows not available for missing device fields`() {
        val text = ReportSummaryBuilder.build(report())
        assertTrue(text.contains("IMEI: No disponible"))
        assertTrue(text.contains("Fabricante: No disponible"))
        assertTrue(text.contains("Color: No disponible"))
    }

    @Test
    fun `includes provided device fields`() {
        val text = ReportSummaryBuilder.build(
            report(
                device = DeviceInfo(
                    manufacturer = "Apple",
                    modelName = "iPhone 13",
                    capacity = "128 GB",
                ),
            ),
        )
        assertTrue(text.contains("Fabricante: Apple"))
        assertTrue(text.contains("Nombre del modelo: iPhone 13"))
        assertTrue(text.contains("Capacidad: 128 GB"))
    }

    @Test
    fun `omits network section when empty and includes it when present`() {
        assertFalse(ReportSummaryBuilder.build(report()).contains("## Red y operador"))

        val withNetwork = ReportSummaryBuilder.build(
            report(network = NetworkInfo(carrier = "Vodafone", country = "Espana")),
        )
        assertTrue(withNetwork.contains("## Red y operador"))
        assertTrue(withNetwork.contains("Operador: Vodafone"))
        assertTrue(withNetwork.contains("Pais: Espana"))
    }

    @Test
    fun `includes extra provider fields`() {
        val text = ReportSummaryBuilder.build(
            report(extraFields = listOf(LookupField("Campo extra", "valor"))),
        )
        assertTrue(text.contains("## Informacion adicional del proveedor"))
        assertTrue(text.contains("Campo extra: valor"))
    }

    @Test
    fun `never includes the raw provider response`() {
        val text = ReportSummaryBuilder.build(
            report(rawResponse = "{\"secreto\":\"no-debe-salir\"}"),
        )
        assertFalse(text.contains("no-debe-salir"))
        assertFalse(text.contains("secreto"))
    }
}
