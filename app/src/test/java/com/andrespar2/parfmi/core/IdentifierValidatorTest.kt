package com.andrespar2.parfmi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdentifierValidatorTest {

    private val validImei = "490154203237518"

    @Test
    fun `detects a valid imei`() {
        val result = IdentifierValidator.validate(validImei)
        assertTrue(result is IdentifierValidation.Valid)
        val identifier = (result as IdentifierValidation.Valid).identifier
        assertEquals(IdentifierType.IMEI, identifier.type)
        assertEquals(validImei, identifier.normalized)
    }

    @Test
    fun `detects a serial number`() {
        val result = IdentifierValidator.validate("C8QH2XYJPLJM")
        assertTrue(result is IdentifierValidation.Valid)
        assertEquals(
            IdentifierType.SERIAL_NUMBER,
            (result as IdentifierValidation.Valid).identifier.type,
        )
    }

    @Test
    fun `ignores separators and normalizes to uppercase`() {
        val result = IdentifierValidator.validate(" 490 154-203237518 ")
        assertTrue(result is IdentifierValidation.Valid)
        val identifier = (result as IdentifierValidation.Valid).identifier
        assertEquals(validImei, identifier.normalized)
        assertEquals(" 490 154-203237518 ", identifier.raw)
    }

    @Test
    fun `normalizes lower case serial numbers`() {
        assertEquals("C8QH2XYJPLJM", IdentifierValidator.normalize("c8qh2xyjpljm"))
    }

    @Test
    fun `reports empty input`() {
        assertEquals(
            IdentifierValidation.Invalid(ValidationError.EMPTY),
            IdentifierValidator.validate("   "),
        )
    }

    @Test
    fun `reports too long input`() {
        assertEquals(
            IdentifierValidation.Invalid(ValidationError.TOO_LONG),
            IdentifierValidator.validate("A".repeat(41)),
        )
    }

    @Test
    fun `reports invalid imei checksum`() {
        assertEquals(
            IdentifierValidation.Invalid(ValidationError.IMEI_CHECKSUM),
            IdentifierValidator.validate("490154203237519"),
        )
    }

    @Test
    fun `reports mistyped imei length for 14 and 16 digit numbers`() {
        assertEquals(
            IdentifierValidation.Invalid(ValidationError.IMEI_LENGTH),
            IdentifierValidator.validate("49015420323751"),
        )
        assertEquals(
            IdentifierValidation.Invalid(ValidationError.IMEI_LENGTH),
            IdentifierValidator.validate("4901542032375188"),
        )
    }

    @Test
    fun `accepts numeric serial numbers that are not imei`() {
        val result = IdentifierValidator.validate("1234567890")
        assertTrue(result is IdentifierValidation.Valid)
        assertEquals(
            IdentifierType.SERIAL_NUMBER,
            (result as IdentifierValidation.Valid).identifier.type,
        )
    }

    @Test
    fun `reports serial numbers outside the accepted length`() {
        assertEquals(
            IdentifierValidation.Invalid(ValidationError.SERIAL_LENGTH),
            IdentifierValidator.validate("ABC123"),
        )
    }

    @Test
    fun `detectType returns unknown for invalid input`() {
        assertEquals(IdentifierType.UNKNOWN, IdentifierValidator.detectType(""))
        assertEquals(IdentifierType.UNKNOWN, IdentifierValidator.detectType("12"))
        assertEquals(IdentifierType.IMEI, IdentifierValidator.detectType(validImei))
        assertEquals(
            IdentifierType.SERIAL_NUMBER,
            IdentifierValidator.detectType("F17WT34LJC6L"),
        )
    }

    @Test
    fun `masked identifier never exposes the full imei`() {
        val identifier = DeviceIdentifier(validImei, validImei, IdentifierType.IMEI)
        val masked = identifier.masked()
        assertEquals(validImei.length, masked.length)
        assertTrue(masked.startsWith("4901"))
        assertTrue(!masked.contains("20323751"))
    }
}
