package com.andrespar2.parfmi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeiValidatorTest {

    /** IMEI de ejemplo con digito de control correcto. */
    private val validImei = "490154203237518"

    /** Mismo IMEI con el ultimo digito alterado. */
    private val invalidChecksumImei = "490154203237519"

    @Test
    fun `accepts a valid 15 digit imei`() {
        assertTrue(ImeiValidator.isValidLength(validImei))
        assertTrue(ImeiValidator.isValidChecksum(validImei))
        assertTrue(ImeiValidator.isComplete(validImei))
    }

    @Test
    fun `rejects an imei with wrong check digit`() {
        assertTrue(ImeiValidator.isValidLength(invalidChecksumImei))
        assertFalse(ImeiValidator.isValidChecksum(invalidChecksumImei))
        assertFalse(ImeiValidator.isComplete(invalidChecksumImei))
    }

    @Test
    fun `validates the placeholder imei used in the ui`() {
        assertTrue(ImeiValidator.isComplete("356938035643809"))
    }

    @Test
    fun `rejects wrong lengths`() {
        assertFalse(ImeiValidator.isValidLength("49015420323751"))
        assertFalse(ImeiValidator.isValidLength("4901542032375188"))
        assertFalse(ImeiValidator.isValidLength(""))
    }

    @Test
    fun `flags numeric values of 14 or 16 digits as mistyped imei`() {
        assertTrue(ImeiValidator.isLikelyMistypedImei("49015420323751"))
        assertTrue(ImeiValidator.isLikelyMistypedImei("4901542032375188"))
        assertFalse(ImeiValidator.isLikelyMistypedImei(validImei))
        assertFalse(ImeiValidator.isLikelyMistypedImei("C8QH2XYJPLJM"))
    }

    @Test
    fun `luhn rejects empty or non numeric input`() {
        assertFalse(ImeiValidator.luhnCheck(""))
        assertFalse(ImeiValidator.luhnCheck("49015420323A518"))
    }

    @Test
    fun `luhn accepts known valid numbers`() {
        assertTrue(ImeiValidator.luhnCheck("79927398713"))
        assertFalse(ImeiValidator.luhnCheck("79927398710"))
    }

    @Test
    fun `flags numeric serial numbers as not imei`() {
        assertFalse(ImeiValidator.isLikelyMistypedImei("12345678"))
        assertEquals(15, ImeiValidator.LENGTH)
    }
}
