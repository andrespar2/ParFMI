package com.andrespar2.parfmi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedactorTest {

    @Test
    fun `mask keeps prefix and suffix and hides the middle`() {
        val masked = Redactor.mask("490154203237518")
        assertEquals(15, masked.length)
        assertEquals("4901*********18", masked)
    }

    @Test
    fun `mask hides short values completely`() {
        assertEquals("*", Redactor.mask("A"))
        assertEquals("******", Redactor.mask("ABCDEF"))
    }

    @Test
    fun `mask returns empty string for empty input`() {
        assertEquals("", Redactor.mask("   "))
    }

    @Test
    fun `redactUrl hides the api key and the imei`() {
        val url = "https://sickw.com/api.php?format=json&key=SUPERSECRET&imei=490154203237518&service=3"
        val redacted = Redactor.redactUrl(url)
        assertFalse(redacted.contains("SUPERSECRET"))
        assertFalse(redacted.contains("490154203237518"))
        assertTrue(redacted.contains("key=***"))
        assertTrue(redacted.contains("imei=***"))
        assertTrue(redacted.contains("service=3"))
    }

    @Test
    fun `redactUrl keeps urls without query string untouched`() {
        val url = "https://sickw.com/api.php"
        assertEquals(url, Redactor.redactUrl(url))
    }

    @Test
    fun `redactHeader hides authorization values`() {
        assertEquals("Authorization: ***", Redactor.redactHeader("Authorization", "Bearer abc"))
        assertEquals("X-Api-Key: ***", Redactor.redactHeader("X-Api-Key", "abc"))
        assertEquals("Content-Type: application/json", Redactor.redactHeader("Content-Type", "application/json"))
    }
}
