package com.andrespar2.parfmi.ui.settings

import com.andrespar2.parfmi.data.LookupFmiUseCase
import com.andrespar2.parfmi.data.model.LookupOutcome
import com.andrespar2.parfmi.data.model.ProviderError
import com.andrespar2.parfmi.data.model.ProviderService
import com.andrespar2.parfmi.data.model.ServiceCatalogResult
import com.andrespar2.parfmi.data.net.ConnectivityChecker
import com.andrespar2.parfmi.data.provider.FmiProvider
import com.andrespar2.parfmi.data.provider.LookupRequest
import com.andrespar2.parfmi.data.settings.CatalogSource
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
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val provider = FakeProvider()

    private val repository = SettingsRepository(
        store = InMemoryKeyValueStore(),
        secretStore = InMemorySecretStore(),
        defaultServices = listOf(
            ProviderService("72", "Apple: operador + MDM", "0.40 USD", defaultService = true),
            ProviderService("3", "iCloud / FMI ON-OFF", "0.02 USD"),
        ),
        defaultProviderId = "sickw",
    )

    private fun viewModel() = SettingsViewModel(
        settingsRepository = repository,
        lookupFmi = LookupFmiUseCase(provider, repository, ConnectivityChecker { true }),
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
    fun `saves the api key and clears the input`() {
        val viewModel = viewModel()
        viewModel.onApiKeyChange("  SECRET  ")
        viewModel.saveApiKey()

        assertTrue(repository.settings.value.apiKeyConfigured)
        assertEquals("SECRET", repository.apiKey())
        assertEquals("", viewModel.apiKeyInput.value)
        assertEquals(SettingsMessage.Saved, viewModel.message.value)
    }

    @Test
    fun `ignores a blank api key`() {
        val viewModel = viewModel()
        viewModel.onApiKeyChange("   ")
        viewModel.saveApiKey()

        assertFalse(repository.settings.value.apiKeyConfigured)
        assertNull(viewModel.message.value)
    }

    @Test
    fun `clears the api key`() {
        repository.saveApiKey("SECRET")
        val viewModel = viewModel()
        viewModel.clearApiKey()

        assertFalse(repository.settings.value.apiKeyConfigured)
        assertEquals(SettingsMessage.KeyCleared, viewModel.message.value)
    }

    @Test
    fun `selects a service`() {
        val viewModel = viewModel()
        viewModel.selectService("3")

        assertEquals("3", repository.settings.value.serviceId)
        assertEquals("iCloud / FMI ON-OFF", repository.settings.value.selectedServiceName)
        assertEquals(SettingsMessage.Saved, viewModel.message.value)
    }

    @Test
    fun `keeps only digits in the custom service id`() {
        val viewModel = viewModel()
        viewModel.onCustomServiceChange("a1b23x")

        assertEquals("123", viewModel.customServiceId.value)
    }

    @Test
    fun `saves a custom service id`() {
        val viewModel = viewModel()
        viewModel.onCustomServiceChange("999")
        viewModel.saveCustomService()

        assertEquals("999", repository.settings.value.serviceId)
        assertEquals("", viewModel.customServiceId.value)
    }

    @Test
    fun `refreshes the catalog and reports how many services arrived`() = runTest(dispatcher) {
        repository.saveApiKey("KEY")
        provider.catalog = ServiceCatalogResult.Success(
            listOf(ProviderService("999", "Servicio en vivo", "1.00 USD")),
        )
        val viewModel = viewModel()

        viewModel.refreshCatalog()
        assertTrue(viewModel.refreshing.value)

        advanceUntilIdle()

        assertEquals(SettingsMessage.CatalogUpdated(1), viewModel.message.value)
        assertFalse(viewModel.refreshing.value)
        assertEquals(CatalogSource.LIVE, repository.settings.value.catalogSource)
    }

    @Test
    fun `reports a catalog refresh failure`() = runTest(dispatcher) {
        repository.saveApiKey("KEY")
        provider.catalog = ServiceCatalogResult.Failure(ProviderError.SERVER_ERROR)
        val viewModel = viewModel()

        viewModel.refreshCatalog()
        advanceUntilIdle()

        assertEquals(SettingsMessage.CatalogFailed, viewModel.message.value)
        assertEquals(CatalogSource.INCLUDED, repository.settings.value.catalogSource)
    }

    @Test
    fun `resets the catalog to the included one`() {
        repository.saveApiKey("KEY")
        repository.saveCatalog(listOf(ProviderService("999", "Servicio en vivo")))
        val viewModel = viewModel()

        viewModel.resetCatalog()

        assertEquals(CatalogSource.INCLUDED, repository.settings.value.catalogSource)
        assertEquals(SettingsMessage.CatalogReset, viewModel.message.value)
    }

    @Test
    fun `consumes the message`() {
        val viewModel = viewModel()
        viewModel.selectService("3")
        viewModel.consumeMessage()

        assertNull(viewModel.message.value)
    }

    private class FakeProvider : FmiProvider {
        override val id = "sickw"
        override val displayName = "Sickw.com"

        var catalog: ServiceCatalogResult = ServiceCatalogResult.Failure(ProviderError.UNKNOWN)

        override suspend fun lookup(request: LookupRequest): LookupOutcome =
            LookupOutcome.Failure(ProviderError.UNKNOWN)

        override suspend fun fetchServices(apiKey: String): ServiceCatalogResult = catalog
    }
}
