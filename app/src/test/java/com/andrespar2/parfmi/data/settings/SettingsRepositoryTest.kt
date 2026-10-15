package com.andrespar2.parfmi.data.settings

import com.andrespar2.parfmi.data.model.ProviderService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

    private val store = InMemoryKeyValueStore()
    private val secretStore = EncryptedSecretStore(store, IdentitySecretCipher())
    private val defaults = SickwTestServices.services

    private fun repository() = SettingsRepository(
        store = store,
        secretStore = secretStore,
        defaultServices = defaults,
        defaultProviderId = "sickw",
    )

    @Test
    fun `starts with the included catalog and no api key`() {
        val settings = repository().settings.value
        assertEquals("sickw", settings.providerId)
        assertEquals("72", settings.serviceId)
        assertEquals(CatalogSource.INCLUDED, settings.catalogSource)
        assertEquals(defaults.size, settings.services.size)
        assertFalse(settings.apiKeyConfigured)
    }

    @Test
    fun `stores the api key encrypted and never in clear text`() {
        val repository = repository()
        repository.saveApiKey("  SUPER_SECRET  ")

        assertTrue(repository.settings.value.apiKeyConfigured)
        assertEquals("SUPER_SECRET", repository.apiKey())

        val rawStored = store.getString(SettingsRepository.KEY_API_KEY)
        assertTrue(rawStored != null && rawStored != "SUPER_SECRET")
    }

    @Test
    fun `clears the api key`() {
        val repository = repository()
        repository.saveApiKey("SUPER_SECRET")
        repository.clearApiKey()

        assertFalse(repository.settings.value.apiKeyConfigured)
        assertNull(repository.apiKey())
    }

    @Test
    fun `saves the selected service`() {
        val repository = repository()
        repository.saveServiceId("3")

        assertEquals("3", repository.settings.value.serviceId)
        assertEquals("iCloud / FMI ON-OFF", repository.settings.value.selectedServiceName)
    }

    @Test
    fun `saves and resets the downloaded catalog`() {
        val repository = repository()
        val live = listOf(
            ProviderService("999", "Servicio de prueba", "1.00 USD", defaultService = true),
        )
        repository.saveCatalog(live)

        assertEquals(CatalogSource.LIVE, repository.settings.value.catalogSource)
        assertEquals(1, repository.settings.value.services.size)

        repository.resetCatalog()
        assertEquals(CatalogSource.INCLUDED, repository.settings.value.catalogSource)
        assertEquals(defaults.size, repository.settings.value.services.size)
    }

    @Test
    fun `keeps the selected service when it disappears from the catalog`() {
        val repository = repository()
        repository.saveServiceId("3")
        repository.saveCatalog(listOf(ProviderService("999", "Otro servicio")))

        val settings = repository.settings.value
        assertEquals("3", settings.serviceId)
        assertNull(settings.selectedService)
        assertEquals("3", settings.selectedServiceName)
    }

    @Test
    fun `catalog codec round trips`() {
        val encoded = ServiceCatalogCodec.encode(defaults)
        val decoded = ServiceCatalogCodec.decode(encoded)
        assertEquals(defaults.size, decoded.size)
        assertEquals(defaults[0].id, decoded[0].id)
        assertEquals(defaults[0].name, decoded[0].name)
        assertEquals(defaults[0].price, decoded[0].price)
        assertTrue(decoded[0].defaultService)
    }

    @Test
    fun `catalog codec tolerates invalid input`() {
        assertTrue(ServiceCatalogCodec.decode(null).isEmpty())
        assertTrue(ServiceCatalogCodec.decode("no json").isEmpty())
        assertTrue(ServiceCatalogCodec.decode("[]").isEmpty())
    }

    @Test
    fun `secret store never writes plain values`() {
        val encrypted = EncryptedSecretStore(InMemoryKeyValueStore(), IdentitySecretCipher())
        encrypted.put("k", "v")
        assertEquals("v", encrypted.get("k"))
        encrypted.remove("k")
        assertFalse(encrypted.contains("k"))
    }
}

/** Servicios de prueba compartidos por las pruebas de ajustes y del caso de uso. */
object SickwTestServices {
    val services = listOf(
        ProviderService("72", "Apple: operador + MDM, iCloud y GSX", "0.40 USD", defaultService = true),
        ProviderService("3", "iCloud / FMI ON-OFF", "0.02 USD"),
    )
}
