package com.andrespar2.parfmi.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SerialNumberValidatorTest {

    @Test
    fun `accepts classic 12 character apple serial numbers`() {
        assertTrue(SerialNumberValidator.isPlausible("C8QH2XYJPLJM"))
    }

    @Test
    fun `accepts modern longer serial numbers`() {
        assertTrue(SerialNumberValidator.isPlausible("F17WT34LJC6L"))
        assertTrue(SerialNumberValidator.isPlausible("H4XK2L9PQRSTUV"))
    }

    @Test
    fun `accepts shorter legacy serial numbers of eight characters`() {
        assertTrue(SerialNumberValidator.isPlausible("AB12CD34"))
    }

    @Test
    fun `accepts numeric serial numbers`() {
        assertTrue(SerialNumberValidator.isPlausible("1234567890"))
    }

    @Test
    fun `rejects values that are too short or too long`() {
        assertFalse(SerialNumberValidator.isPlausible("ABC1234"))
        assertFalse(SerialNumberValidator.isPlausible("A".repeat(21)))
    }

    @Test
    fun `rejects characters that are not alphanumeric`() {
        assertFalse(SerialNumberValidator.hasValidCharacters("C8QH-2XYJ"))
        assertFalse(SerialNumberValidator.hasValidCharacters("C8QH 2XYJ"))
        assertFalse(SerialNumberValidator.hasValidCharacters("C8QH_2XYJ"))
    }

    @Test
    fun `exposes the accepted length range`() {
        assertTrue(SerialNumberValidator.MIN_LENGTH == 8)
        assertTrue(SerialNumberValidator.MAX_LENGTH == 20)
    }
}
