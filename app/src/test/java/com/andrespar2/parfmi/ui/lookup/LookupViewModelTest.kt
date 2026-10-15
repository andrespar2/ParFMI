package com.andrespar2.parfmi.ui.lookup

import com.andrespar2.parfmi.core.DeviceIdentifier
import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.core.ValidationError
import com.andrespar2.parfmi.data.LookupFmiUseCase
import com.andrespar2.parfmi.data.model.FmiReport
import com.andrespar2.parfmi.data.model.LookupOutcome
import com.andrespar2.parfmi.data.model.ProviderError
import com.andrespar2.parfmi.data.model.ProviderService
import com.andrespar2.parfmi.data.model.ServiceCatalogResult
import com.andrespar2.parfmi.data.net.ConnectivityChecker
import com.andrespar2.parfmi.data.provider.FmiProvider
import com.andrespar2.parfmi.data.provider.LookupRequest
import com.andrespar2.parfmi.data.settings.InMemoryKeyValueStore
import com.andrespar2.parfmi.data.settings.InMemorySecretStore
import com.andrespar2.parfmi.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LookupViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val validImei = "490154203237518"

    private val provider = FakeProvider()
    private val defaults = listOf(
        ProviderService("72", "Apple: operador + MDM", "0.40 USD", defaultService = true),
    )

    private fun repository() = SettingsRepository(
        store = InMemoryKeyValueStore(),
        secretStore = InMemorySecretStore(),
        defaultServices = defaults,
        defaultProviderId = "sickw",
    )

    private fun viewModel(repository: SettingsRepository, online: Boolean = true) = LookupViewModel(
        lookupFmi = LookupFmiUseCase(provider, repository, ConnectivityChecker { online }),
        settingsRepository = repository,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invalid input sets the error and does not navigate`() {
        val repository = repository()
        repository.saveApiKey("KEY")
        val viewModel = viewModel(repository)
        var navigated = false

        viewModel.onInputChange("123")
        viewModel.submit { navigated = true }

        assertFalse(navigated)
        assertEquals(ValidationError.SERIAL_LENGTH, viewModel.inputError.value)
        assertTrue(viewModel.status.value is LookupStatus.Idle)
    }

    @Test
    fun `clears the input error when the user keeps typing`() {
        val repository = repository()
        repository.saveApiKey("KEY")
        val viewModel = viewModel(repository)

        viewModel.onInputChange("123")
        viewModel.submit { }
        assertEquals(ValidationError.SERIAL_LENGTH, viewModel.inputError.value)

        viewModel.onInputChange("1234")
        assertNull(viewModel.inputError.value)
    }

    @Test
    fun `missing api key navigates to the result with a failure`() {
        val viewModel = viewModel(repository())
        var navigated = false

        viewModel.onInputChange(validImei)
        viewModel.submit { navigated = true }

        assertTrue(navigated)
        val outcome = (viewModel.status.value as LookupStatus.Done).outcome
        assertEquals(ProviderError.NO_PROVIDER_CONFIGURED, (outcome as LookupOutcome.Failure).error)
    }

    @Test
    fun `offline lookup navigates to the result with a failure`() {
        val repository = repository()
        repository.saveApiKey("KEY")
        val viewModel = viewModel(repository, online = false)
        var navigated = false

        viewModel.onInputChange(validImei)
        viewModel.submit { navigated = true }

        assertTrue(navigated)
        val outcome = (viewModel.status.value as LookupStatus.Done).outcome
        assertEquals(ProviderError.NO_NETWORK, (outcome as LookupOutcome.Failure).error)
    }

    @Test
    fun `successful lookup shows loading then the report`() = runTest(dispatcher) {
        val repository = repository()
        repository.saveApiKey("KEY")
        provider.outcome = LookupOutcome.Success(
            FmiReport(
                identifier = DeviceIdentifier(validImei, validImei, IdentifierType.IMEI),
                providerId = "sickw",
                providerName = "Sickw.com",
                serviceId = "72",
                queriedAtMillis = 1,
            ),
        )
        val viewModel = viewModel(repository)
        var navigated = false

        viewModel.onInputChange(validImei)
        viewModel.submit { navigated = true }

        assertTrue(navigated)
        assertTrue(viewModel.status.value is LookupStatus.Loading)

        advanceUntilIdle()

        val done = viewModel.status.value as LookupStatus.Done
        assertTrue(done.outcome is LookupOutcome.Success)
        assertEquals(1, provider.lookupCalls)
    }

    @Test
    fun `reset returns to idle`() = runTest(dispatcher) {
        val repository = repository()
        repository.saveApiKey("KEY")
        val viewModel = viewModel(repository)

        viewModel.onInputChange(validImei)
        viewModel.submit { }
        advanceUntilIdle()
        assertTrue(viewModel.status.value is LookupStatus.Done)

        viewModel.resetResult()
        assertTrue(viewModel.status.value is LookupStatus.Idle)
    }

    @Test
    fun `detects the identifier type live and rejects unknown formats`() {
        val viewModel = viewModel(repository())

        viewModel.onInputChange(validImei)
        assertEquals(IdentifierType.IMEI, viewModel.detectedType())

        viewModel.onInputChange("C8QH2XYJPLJM")
        assertEquals(IdentifierType.SERIAL_NUMBER, viewModel.detectedType())

        viewModel.onInputChange("!!!")
        assertNull(viewModel.detectedType())
    }

    private class FakeProvider : FmiProvider {
        override val id = "sickw"
        override val displayName = "Sickw.com"

        var lookupCalls = 0
        var outcome: LookupOutcome = LookupOutcome.Failure(ProviderError.UNKNOWN)

        override suspend fun lookup(request: LookupRequest): LookupOutcome {
            lookupCalls++
            return outcome
        }

        override suspend fun fetchServices(apiKey: String): ServiceCatalogResult =
            ServiceCatalogResult.Failure(ProviderError.UNKNOWN)
    }
}
