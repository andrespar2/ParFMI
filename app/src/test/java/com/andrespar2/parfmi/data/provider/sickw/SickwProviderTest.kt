package com.andrespar2.parfmi.data.provider.sickw

import com.andrespar2.parfmi.core.DeviceIdentifier
import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.data.model.LookupOutcome
import com.andrespar2.parfmi.data.model.ProviderError
import com.andrespar2.parfmi.data.model.ServiceCatalogResult
import com.andrespar2.parfmi.data.net.OkHttpEngine
import com.andrespar2.parfmi.data.provider.LookupRequest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Pruebas del proveedor contra un servidor HTTP local que reproduce el formato
 * documentado de Sickw. No se realiza ninguna consulta real ni se consume
 * saldo, y por tanto no se necesita API key.
 */
class SickwProviderTest {

    private lateinit var server: MockWebServer

    private val identifier = DeviceIdentifier(
        raw = "356938035643809",
        normalized = "356938035643809",
        type = IdentifierType.IMEI,
    )

    private val request = LookupRequest(
        identifier = identifier,
        serviceId = "72",
        serviceName = "Apple: operador + MDM, iCloud y GSX",
        apiKey = "TEST_KEY",
    )

    private fun provider(
        client: OkHttpClient = OkHttpClient.Builder().build(),
    ) = SickwProvider(http = OkHttpEngine(client), baseUrl = server.url("/api.php").toString())

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `sends the documented query parameters`() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"result":{"FMI":"ON"},"status":"success"}"""
            )
        )

        val outcome = provider().lookup(request)
        assertTrue(outcome is LookupOutcome.Success)

        val recorded = server.takeRequest()
        val path = recorded.path.orEmpty()
        assertTrue(path.contains("format=json"))
        assertTrue(path.contains("key=TEST_KEY"))
        assertTrue(path.contains("imei=356938035643809"))
        assertTrue(path.contains("service=72"))
        assertEquals("GET", recorded.method)
    }

    @Test
    fun `builds a report from a successful response`() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                {"result":{"Manufacturer":"APPLE","Model Code":"IPHONE 6 PLUS (A1524)",
                "Model Name":"IPHONE 6 PLUS","FMI":"OFF"},"imei":"356938035643809",
                "balance":"878.75","price":"0.40","service":"72","id":"76369088",
                "status":"success"}
                """.trimIndent()
            )
        )

        val outcome = provider().lookup(request)
        assertTrue(outcome is LookupOutcome.Success)
        val report = (outcome as LookupOutcome.Success).report
        assertEquals("APPLE", report.device.manufacturer)
        assertEquals("878.75", report.balance)
        assertEquals("72", report.serviceId)
        assertEquals("Sickw.com", report.providerName)
        assertEquals(
            com.andrespar2.parfmi.data.model.FmiStatus.OFF,
            report.security.fmi,
        )
    }

    @Test
    fun `maps provider messages to errors`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"result":"Invalid API key","status":"error"}"""))
        val outcome = provider().lookup(request)
        assertTrue(outcome is LookupOutcome.Failure)
        assertEquals(
            ProviderError.UNAUTHORIZED,
            (outcome as LookupOutcome.Failure).error,
        )
    }

    @Test
    fun `maps http error codes`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(403).setBody("forbidden"))
        val forbidden = provider().lookup(request)
        assertTrue(forbidden is LookupOutcome.Failure)
        assertEquals(
            ProviderError.UNAUTHORIZED,
            (forbidden as LookupOutcome.Failure).error,
        )

        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))
        val serverError = provider().lookup(request)
        assertEquals(
            ProviderError.SERVER_ERROR,
            (serverError as LookupOutcome.Failure).error,
        )
    }

    @Test
    fun `maps malformed responses to a parse error`() = runBlocking {
        server.enqueue(MockResponse().setBody("<html>error</html>"))
        val outcome = provider().lookup(request)
        assertTrue(outcome is LookupOutcome.Failure)
        assertEquals(ProviderError.PARSE_ERROR, (outcome as LookupOutcome.Failure).error)
    }

    @Test
    fun `maps read timeouts`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val fastClient = OkHttpClient.Builder()
            .readTimeout(400, TimeUnit.MILLISECONDS)
            .callTimeout(800, TimeUnit.MILLISECONDS)
            .build()

        val outcome = provider(fastClient).lookup(request)
        assertTrue(outcome is LookupOutcome.Failure)
        assertEquals(ProviderError.TIMEOUT, (outcome as LookupOutcome.Failure).error)
    }

    @Test
    fun `does not leak the api key into the failure detail`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))
        val outcome = provider().lookup(request)
        val detail = (outcome as LookupOutcome.Failure).detail.orEmpty()
        assertFalse(detail.contains("TEST_KEY"))
        assertFalse(detail.contains("356938035643809"))
    }

    @Test
    fun `fetchServices returns the live catalog`() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"Service List":[{"service":"3","name":"iCLOUD ON/OFF","price":"0.02"},
                {"service":"72","name":"APPLE CARRIER + MDM & iCLOUD & GSX STATUS","price":"0.40"}]}"""
            )
        )

        val result = provider().fetchServices("TEST_KEY")
        assertTrue(result is ServiceCatalogResult.Success)
        val services = (result as ServiceCatalogResult.Success).services
        assertEquals(2, services.size)
        assertEquals("3", services.first().id)

        val path = server.takeRequest().path.orEmpty()
        assertTrue(path.contains("key=TEST_KEY"))
        assertFalse(path.contains("imei="))
    }

    @Test
    fun `fetchServices reports errors when the provider rejects the call`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"result":"Invalid API key","status":"error"}"""))
        val result = provider().fetchServices("TEST_KEY")
        assertTrue(result is ServiceCatalogResult.Failure)
        assertEquals(
            ProviderError.UNAUTHORIZED,
            (result as ServiceCatalogResult.Failure).error,
        )
    }

    @Test
    fun `fetchServices reports a parse error when no list is returned`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"result":{"Model":"iPhone"},"status":"success"}"""))
        val result = provider().fetchServices("TEST_KEY")
        assertTrue(result is ServiceCatalogResult.Failure)
    }
}
