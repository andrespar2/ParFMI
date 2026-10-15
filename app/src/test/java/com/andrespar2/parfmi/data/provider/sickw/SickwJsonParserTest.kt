package com.andrespar2.parfmi.data.provider.sickw

import com.andrespar2.parfmi.data.model.ProviderError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ejemplos basados en la forma documentada por Sickw:
 * `{"result": {...}, "imei": "...", "balance": "...", "price": "...",
 *   "service": "...", "id": "...", "status": "success"}`
 */
class SickwJsonParserTest {

    private val successBody = """
        {
          "result": {
            "IMEI": "356938035643809",
            "Manufacturer": "APPLE",
            "Model Code": "IPHONE 6 PLUS (A1524)",
            "Model Name": "IPHONE 6 PLUS"
          },
          "imei": "356938035643809",
          "balance": "878.75",
          "price": "0",
          "service": "203",
          "id": "76369088",
          "status": "success"
        }
    """.trimIndent()

    @Test
    fun `parses a successful response`() {
        val parsed = SickwJsonParser.parse(successBody)
        assertTrue(parsed is SickwParseResult.Data)
        val payload = (parsed as SickwParseResult.Data).payload
        assertEquals("success", payload.status)
        assertEquals("878.75", payload.balance)
        assertEquals("203", payload.service)
        assertEquals("76369088", payload.orderId)
        assertEquals("APPLE", payload.fields["Manufacturer"])
        assertEquals(4, payload.fields.size)
    }

    @Test
    fun `flattens nested objects and arrays`() {
        val body = """
            {"result":{"Device":{"Model":"iPhone 13","Capacity":"256 GB"},
            "Extras":["uno","dos"]},"status":"success"}
        """.trimIndent()
        val payload = (SickwJsonParser.parse(body) as SickwParseResult.Data).payload
        assertEquals("iPhone 13", payload.fields["Device.Model"])
        assertEquals("256 GB", payload.fields["Device.Capacity"])
        assertEquals("uno", payload.fields["Extras 1"])
        assertEquals("dos", payload.fields["Extras 2"])
    }

    @Test
    fun `parses the service list`() {
        val body = """
            {"Service List":[{"service":"3","name":"iCLOUD ON/OFF","price":"0.02"},
            {"service":"72","name":"APPLE CARRIER + MDM & iCLOUD & GSX STATUS","price":"0.40"}]}
        """.trimIndent()
        val parsed = SickwJsonParser.parse(body)
        assertTrue(parsed is SickwParseResult.ServiceList)
        val services = (parsed as SickwParseResult.ServiceList).services
        assertEquals(2, services.size)
        assertEquals("3", services[0].id)
        assertEquals("iCLOUD ON/OFF", services[0].name)
        assertEquals("0.02", services[0].price)
        assertTrue(services[1].defaultService)
    }

    @Test
    fun `treats a text result with error status as a provider message`() {
        val body = """{"result":"Invalid API key","status":"error"}"""
        val parsed = SickwJsonParser.parse(body)
        assertTrue(parsed is SickwParseResult.Message)
        assertEquals(
            ProviderError.UNAUTHORIZED,
            SickwMessageClassifier.classify((parsed as SickwParseResult.Message).message),
        )
    }

    @Test
    fun `keeps a text payload when the provider reports success`() {
        val body = """{"result":"iPhone 12 Pro, 128 GB","status":"success","service":"3"}"""
        val payload = (SickwJsonParser.parse(body) as SickwParseResult.Data).payload
        assertEquals("iPhone 12 Pro, 128 GB", payload.textPayload)
        assertTrue(payload.fields.isEmpty())
    }

    @Test
    fun `reports malformed json`() {
        val parsed = SickwJsonParser.parse("{no es json}")
        assertTrue(parsed is SickwParseResult.Malformed)
    }

    @Test
    fun `reports empty body`() {
        assertTrue(SickwJsonParser.parse("") is SickwParseResult.Malformed)
        assertTrue(SickwJsonParser.parse("   ") is SickwParseResult.Malformed)
    }

    @Test
    fun `reports json that is not an object`() {
        assertTrue(SickwJsonParser.parse("[1,2,3]") is SickwParseResult.Malformed)
    }

    @Test
    fun `ignores null values`() {
        val body = """{"result":{"Model":"iPhone","Color":null},"status":"success"}"""
        val payload = (SickwJsonParser.parse(body) as SickwParseResult.Data).payload
        assertEquals("iPhone", payload.fields["Model"])
        assertEquals(1, payload.fields.size)
    }

    @Test
    fun `classifies provider messages`() {
        assertEquals(ProviderError.UNAUTHORIZED, SickwMessageClassifier.classify("Invalid API key"))
        assertEquals(
            ProviderError.INSUFFICIENT_BALANCE,
            SickwMessageClassifier.classify("Insufficient balance"),
        )
        assertEquals(ProviderError.NOT_FOUND, SickwMessageClassifier.classify("IMEI not found"))
        assertEquals(ProviderError.RATE_LIMITED, SickwMessageClassifier.classify("Rate limit reached"))
        assertEquals(ProviderError.TIMEOUT, SickwMessageClassifier.classify("timeout"))
        assertEquals(ProviderError.REQUEST_REJECTED, SickwMessageClassifier.classify("otro motivo"))
    }
}
