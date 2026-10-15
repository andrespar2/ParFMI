package com.andrespar2.parfmi.data.provider.sickw

import com.andrespar2.parfmi.core.DeviceIdentifier
import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.data.model.ActivationLockStatus
import com.andrespar2.parfmi.data.model.ActivationState
import com.andrespar2.parfmi.data.model.BlacklistStatus
import com.andrespar2.parfmi.data.model.CoverageStatus
import com.andrespar2.parfmi.data.model.FmiStatus
import com.andrespar2.parfmi.data.model.IcloudState
import com.andrespar2.parfmi.data.model.MdmStatus
import com.andrespar2.parfmi.data.model.SimLockStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SickwFieldMapperTest {

    private val mapper = SickwFieldMapper()

    private val imeiIdentifier = DeviceIdentifier(
        raw = "356938035643809",
        normalized = "356938035643809",
        type = IdentifierType.IMEI,
    )

    private val serialIdentifier = DeviceIdentifier(
        raw = "F17WT34LJC6L",
        normalized = "F17WT34LJC6L",
        type = IdentifierType.SERIAL_NUMBER,
    )

    private fun payloadOf(vararg fields: Pair<String, String>, balance: String? = null) =
        SickwPayload(
            fields = linkedMapOf(*fields),
            balance = balance,
            status = "success",
        )

    @Test
    fun `classifies labels by specificity`() {
        assertEquals(FieldTarget.ACTIVATION_LOCK, mapper.classifyLabel("Activation Lock Status"))
        assertEquals(FieldTarget.ACTIVATION_DATE, mapper.classifyLabel("Activation Date"))
        assertEquals(FieldTarget.ACTIVATION_STATE, mapper.classifyLabel("Activation Status"))
        assertEquals(FieldTarget.SIM_LOCK, mapper.classifyLabel("Carrier Lock"))
        assertEquals(FieldTarget.SIM_LOCK, mapper.classifyLabel("Simlock"))
        assertEquals(FieldTarget.CARRIER, mapper.classifyLabel("Carrier"))
        assertEquals(FieldTarget.MODEL_CODE, mapper.classifyLabel("Model Code"))
        assertEquals(FieldTarget.MODEL_NAME, mapper.classifyLabel("Model Name"))
        assertEquals(FieldTarget.MODEL, mapper.classifyLabel("Model"))
        assertEquals(FieldTarget.PART_NUMBER, mapper.classifyLabel("Part Number"))
        assertEquals(FieldTarget.SERIAL, mapper.classifyLabel("Serial Number"))
        assertEquals(FieldTarget.COVERAGE, mapper.classifyLabel("Warranty Status"))
        assertEquals(FieldTarget.COVERAGE, mapper.classifyLabel("Repairs and Service Coverage"))
        assertEquals(FieldTarget.TECHNICAL_SUPPORT, mapper.classifyLabel("Telephone Technical Support"))
        assertEquals(FieldTarget.MDM, mapper.classifyLabel("MDM Status"))
        assertEquals(FieldTarget.BLACKLIST, mapper.classifyLabel("Blacklist Status"))
        assertEquals(FieldTarget.ICLOUD, mapper.classifyLabel("iCloud Status"))
        assertEquals(FieldTarget.FMI, mapper.classifyLabel("FMI"))
        assertEquals(FieldTarget.EXTRA, mapper.classifyLabel("GSX Status"))
        assertEquals(FieldTarget.EXTRA, mapper.classifyLabel(""))
    }

    @Test
    fun `maps a complete apple response`() {
        val report = mapper.toReport(
            payload = payloadOf(
                "Carrier" to "AT&T",
                "Country" to "United States",
                "MDM Status" to "Clean",
                "iCloud Status" to "Clean",
                "GSX Status" to "Active",
                "Model" to "iPhone 12 Pro",
                "Model Code" to "A2341",
                "Manufacturer" to "Apple",
                "Color" to "Graphite",
                "Capacity" to "128 GB",
                "Serial Number" to "F17WT34LJC6L",
                "Activation Status" to "Activated",
                "Activation Lock" to "OFF",
                "Warranty Status" to "Out Of Warranty (No Coverage)",
                "Estimated Purchase Date" to "2020-10-23",
                "Simlock" to "Unlocked",
                "FMI" to "OFF",
                "Blacklist Status" to "Clean",
                "Part Number" to "MGLD3LL/A",
                balance = "12.34",
            ),
            identifier = imeiIdentifier,
            serviceId = "72",
            serviceName = "Apple: operador + MDM, iCloud y GSX",
            queriedAtMillis = 1_700_000_000_000,
            rawResponse = "{}",
        )

        assertEquals(FmiStatus.OFF, report.security.fmi)
        assertEquals(IcloudState.CLEAN, report.security.icloudState)
        assertEquals(ActivationLockStatus.UNLOCKED, report.security.activationLock)
        assertEquals(ActivationState.ACTIVATED, report.security.activationState)
        assertEquals(BlacklistStatus.CLEAN, report.security.blacklist)
        assertEquals(MdmStatus.NOT_ENROLLED, report.security.mdm)
        assertEquals(SimLockStatus.UNLOCKED, report.security.simLock)

        assertEquals("iPhone 12 Pro", report.device.model)
        assertEquals("A2341", report.device.modelCode)
        assertEquals("Apple", report.device.manufacturer)
        assertEquals("Graphite", report.device.color)
        assertEquals("128 GB", report.device.capacity)
        assertEquals("F17WT34LJC6L", report.device.serialNumber)
        assertEquals("MGLD3LL/A", report.device.partNumber)
        assertEquals("356938035643809", report.device.imei)

        assertEquals(CoverageStatus.OUT_OF_WARRANTY, report.coverage.status)
        assertEquals("2020-10-23", report.coverage.purchaseDate)

        assertEquals("AT&T", report.network.carrier)
        assertEquals("United States", report.network.country)

        assertEquals("12.34", report.balance)
        assertEquals("72", report.serviceId)
        assertEquals("Sickw.com", report.providerName)

        // GSX no es un campo estructurado: se conserva como informacion adicional.
        assertTrue(report.extraFields.any { it.label == "GSX Status" })
    }

    @Test
    fun `keeps unknown states as unknown instead of off`() {
        val report = mapper.toReport(
            payload = payloadOf("Model" to "iPhone 11"),
            identifier = imeiIdentifier,
            serviceId = "203",
            serviceName = "Marca y modelo",
            queriedAtMillis = 0,
            rawResponse = null,
        )
        assertEquals(FmiStatus.UNKNOWN, report.security.fmi)
        assertEquals(ActivationLockStatus.UNKNOWN, report.security.activationLock)
        assertEquals(ActivationState.UNKNOWN, report.security.activationState)
        assertEquals(IcloudState.UNKNOWN, report.security.icloudState)
        assertEquals(BlacklistStatus.UNKNOWN, report.security.blacklist)
        assertEquals(MdmStatus.UNKNOWN, report.security.mdm)
        assertEquals(SimLockStatus.UNKNOWN, report.security.simLock)
        assertEquals(CoverageStatus.UNKNOWN, report.coverage.status)
        assertNull(report.coverage.purchaseDate)
        assertNull(report.network.carrier)
        assertNull(report.device.serialNumber)
    }

    @Test
    fun `derives fmi from activation lock and icloud only`() {
        val locked = mapper.toReport(
            payload = payloadOf("Activation Lock" to "ON"),
            identifier = imeiIdentifier,
            serviceId = "101",
            serviceName = null,
            queriedAtMillis = 0,
            rawResponse = null,
        )
        assertEquals(FmiStatus.ON, locked.security.fmi)

        val unlocked = mapper.toReport(
            payload = payloadOf("Activation Lock" to "OFF"),
            identifier = imeiIdentifier,
            serviceId = "101",
            serviceName = null,
            queriedAtMillis = 0,
            rawResponse = null,
        )
        assertEquals(FmiStatus.OFF, unlocked.security.fmi)

        val lost = mapper.toReport(
            payload = payloadOf("iCloud Status" to "LOST / ERASED"),
            identifier = imeiIdentifier,
            serviceId = "66",
            serviceName = null,
            queriedAtMillis = 0,
            rawResponse = null,
        )
        assertEquals(FmiStatus.ON, lost.security.fmi)
        assertEquals(IcloudState.LOST, lost.security.icloudState)
    }

    @Test
    fun `ignores missing values and never invents data`() {
        val report = mapper.toReport(
            payload = payloadOf(
                "Model" to "N/A",
                "Color" to "-",
                "Carrier" to "   ",
                "FMI" to "N/A",
                "Country" to "Spain",
            ),
            identifier = imeiIdentifier,
            serviceId = "203",
            serviceName = null,
            queriedAtMillis = 0,
            rawResponse = null,
        )
        assertNull(report.device.model)
        assertNull(report.device.color)
        assertNull(report.network.carrier)
        assertEquals(FmiStatus.UNKNOWN, report.security.fmi)
        assertEquals("Spain", report.network.country)
        assertTrue(report.extraFields.isEmpty())
    }

    @Test
    fun `keeps unrecognized labels as additional information`() {
        val report = mapper.toReport(
            payload = payloadOf(
                "Chimaera Detect" to "Not detected",
                "Demo Device" to "No",
                "Loaner Device" to "No",
            ),
            identifier = imeiIdentifier,
            serviceId = "151",
            serviceName = null,
            queriedAtMillis = 0,
            rawResponse = null,
        )
        assertEquals(3, report.extraFields.size)
        assertEquals("Chimaera Detect", report.extraFields[0].label)
        assertEquals("Not detected", report.extraFields[0].value)
    }

    @Test
    fun `uses the queried identifier when the provider does not echo it`() {
        val report = mapper.toReport(
            payload = payloadOf("Model" to "iPhone SE"),
            identifier = serialIdentifier,
            serviceId = "26",
            serviceName = null,
            queriedAtMillis = 0,
            rawResponse = null,
        )
        assertEquals("F17WT34LJC6L", report.device.serialNumber)
        assertNull(report.device.imei)
    }

    @Test
    fun `includes the text payload as additional information`() {
        val report = mapper.toReport(
            payload = SickwPayload(fields = emptyMap(), textPayload = "iPhone 12 Pro, 128 GB"),
            identifier = imeiIdentifier,
            serviceId = "3",
            serviceName = null,
            queriedAtMillis = 0,
            rawResponse = null,
        )
        assertTrue(report.extraFields.any { it.value == "iPhone 12 Pro, 128 GB" })
    }

    @Test
    fun `sets the query timestamp and service name`() {
        val report = mapper.toReport(
            payload = payloadOf("FMI" to "ON"),
            identifier = imeiIdentifier,
            serviceId = "3",
            serviceName = "iCloud / FMI ON-OFF",
            queriedAtMillis = 42_000,
            rawResponse = "{\"a\":1}",
        )
        assertEquals(42_000L, report.queriedAtMillis)
        assertEquals("iCloud / FMI ON-OFF", report.serviceName)
        assertEquals("{\"a\":1}", report.rawResponse)
        assertEquals("IMEI", report.identifierLabel)
    }
}
