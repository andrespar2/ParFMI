package com.andrespar2.parfmi.data

import com.andrespar2.parfmi.core.DeviceIdentifier
import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.core.ValidationError
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
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LookupFmiUseCaseTest {

    private val validImei = "490154203237518"

    private val provider = FakeProvider()
    private val store = InMemoryKeyValueStore()
    private val secretStore = InMemorySecretStore()

    private val defaults = listOf(
        ProviderService("72", "Apple: operador + MDM, iCloud y GSX", "0.40 USD", defaultService = true),
    )

    private fun settingsRepository() = SettingsRepository(
        store = store,
        secretStore = secretStore,
        defaultServices = defaults,
        defaultProviderId = "sickw",
    )

    private fun useCase(online: Boolean = true, repository: SettingsRepository) =
        LookupFmiUseCase(
            provider = provider,
            settingsRepository = repository,
            connectivityChecker = ConnectivityChecker { online },
        )

    @Test
    fun `rejects invalid input before calling the provider`() {
        val repository = settingsRepository()
        repository.saveApiKey("KEY")
        val check = useCase(repository = repository).preCheck("123")

        assertTrue(check is LookupFmiUseCase.PreCheck.InvalidInput)
        assertEquals(
            ValidationError.SERIAL_LENGTH,
            (check as LookupFmiUseCase.PreCheck.InvalidInput).error,
        )
        assertEquals(0, provider.lookupCalls)
    }

    @Test
    fun `rejects the lookup when no api key is configured`() {
        val check = useCase(repository = settingsRepository()).preCheck(validImei)

        assertTrue(check is LookupFmiUseCase.PreCheck.Rejected)
        assertEquals(
            ProviderError.NO_PROVIDER_CONFIGURED,
            (check as LookupFmiUseCase.PreCheck.Rejected).outcome.error,
        )
    }

    @Test
    fun `rejects the lookup when there is no connection`() {
        val repository = settingsRepository()
        repository.saveApiKey("KEY")

        val check = useCase(online = false, repository = repository).preCheck(validImei)
        assertTrue(check is LookupFmiUseCase.PreCheck.Rejected)
        assertEquals(
            ProviderError.NO_NETWORK,
            (check as LookupFmiUseCase.PreCheck.Rejected).outcome.error,
        )
    }

    @Test
    fun `builds a request with the selected service and the api key`() {
        val repository = settingsRepository()
        repository.saveApiKey("KEY")
        repository.saveServiceId("72")

        val check = useCase(repository = repository).preCheck(" 490 154-203237518 ")
        assertTrue(check is LookupFmiUseCase.PreCheck.Ready)
        val request = (check as LookupFmiUseCase.PreCheck.Ready).request
        assertEquals(validImei, request.identifier.normalized)
        assertEquals("72", request.serviceId)
        assertEquals("KEY", request.apiKey)
        assertEquals("Apple: operador + MDM, iCloud y GSX", request.serviceName)
    }

    @Test
    fun `executes a successful lookup`() = runBlocking {
        val repository = settingsRepository()
        repository.saveApiKey("KEY")
        provider.outcome = LookupOutcome.Success(
            com.andrespar2.parfmi.data.model.FmiReport(
                identifier = DeviceIdentifier(validImei, validImei, IdentifierType.IMEI),
                providerId = "sickw",
                providerName = "Sickw.com",
                serviceId = "72",
                queriedAtMillis = 1,
            )
        )

        val outcome = useCase(repository = repository).execute(validImei)
        assertTrue(outcome is LookupOutcome.Success)
        assertEquals(1, provider.lookupCalls)
    }

    @Test
    fun `propagates provider failures`() = runBlocking {
        val repository = settingsRepository()
        repository.saveApiKey("KEY")
        provider.outcome = LookupOutcome.Failure(ProviderError.NOT_FOUND, "sin datos")

        val outcome = useCase(repository = repository).execute(validImei)
        assertTrue(outcome is LookupOutcome.Failure)
        assertEquals(ProviderError.NOT_FOUND, (outcome as LookupOutcome.Failure).error)
    }

    @Test
    fun `stores the catalog after a successful refresh`() = runBlocking {
        val repository = settingsRepository()
        repository.saveApiKey("KEY")
        provider.catalog = ServiceCatalogResult.Success(
            listOf(ProviderService("3", "iCLOUD ON/OFF", "0.02 USD"))
        )

        val result = useCase(repository = repository).refreshCatalog()
        assertTrue(result is ServiceCatalogResult.Success)
        assertEquals(1, repository.settings.value.services.size)
    }

    @Test
    fun `refresh requires an api key`() = runBlocking {
        val result = useCase(repository = settingsRepository()).refreshCatalog()
        assertTrue(result is ServiceCatalogResult.Failure)
        assertEquals(
            ProviderError.NO_PROVIDER_CONFIGURED,
            (result as ServiceCatalogResult.Failure).error,
        )
    }

    @Test
    fun `refresh requires connectivity`() = runBlocking {
        val repository = settingsRepository()
        repository.saveApiKey("KEY")
        val result = useCase(online = false, repository = repository).refreshCatalog()
        assertEquals(
            ProviderError.NO_NETWORK,
            (result as ServiceCatalogResult.Failure).error,
        )
    }

    private class FakeProvider : FmiProvider {
        override val id = "sickw"
        override val displayName = "Sickw.com"

        var lookupCalls = 0
        var outcome: LookupOutcome = LookupOutcome.Failure(ProviderError.UNKNOWN)
        var catalog: ServiceCatalogResult = ServiceCatalogResult.Failure(ProviderError.UNKNOWN)

        override suspend fun lookup(request: LookupRequest): LookupOutcome {
            lookupCalls++
            return outcome
        }

        override suspend fun fetchServices(apiKey: String): ServiceCatalogResult = catalog
    }
}
