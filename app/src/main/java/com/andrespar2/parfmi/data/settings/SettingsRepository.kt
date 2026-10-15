package com.andrespar2.parfmi.data.settings

import com.andrespar2.parfmi.data.model.ProviderService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias de la app.
 *
 * La API key nunca se guarda aqui: vive en un [SecretStore] cifrado. En este
 * almacen solo se conserva que existe o no, ademas del servicio elegido y el
 * catalogo de servicios (sin datos personales).
 */
class SettingsRepository(
    private val store: KeyValueStore,
    private val secretStore: SecretStore,
    private val defaultServices: List<ProviderService>,
    private val defaultProviderId: String,
) {

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun load(): AppSettings {
        val storedCatalog = ServiceCatalogCodec.decode(store.getString(KEY_CATALOG))
        val services = storedCatalog.ifEmpty { defaultServices }
        val catalogSource =
            if (storedCatalog.isNotEmpty()) CatalogSource.LIVE else CatalogSource.INCLUDED

        val serviceId = store.getString(KEY_SERVICE)?.takeIf { it.isNotBlank() }
            ?: services.firstOrNull { it.defaultService }?.id
            ?: services.firstOrNull()?.id
            ?: DEFAULT_SERVICE_ID

        return AppSettings(
            providerId = store.getString(KEY_PROVIDER)?.takeIf { it.isNotBlank() }
                ?: defaultProviderId,
            serviceId = serviceId,
            apiKeyConfigured = hasApiKey(),
            services = services,
            catalogSource = catalogSource,
        )
    }

    fun refresh() {
        _settings.value = load()
    }

    fun apiKey(): String? = secretStore.get(KEY_API_KEY)?.takeIf { it.isNotBlank() }

    fun hasApiKey(): Boolean = apiKey() != null

    fun saveApiKey(apiKey: String) {
        secretStore.put(KEY_API_KEY, apiKey.trim())
        refresh()
    }

    fun clearApiKey() {
        secretStore.remove(KEY_API_KEY)
        refresh()
    }

    fun saveServiceId(serviceId: String) {
        store.putString(KEY_SERVICE, serviceId.trim())
        refresh()
    }

    fun saveCatalog(services: List<ProviderService>) {
        if (services.isEmpty()) return
        store.putString(KEY_CATALOG, ServiceCatalogCodec.encode(services))
        refresh()
    }

    /** Vuelve al catalogo incluido en la app. */
    fun resetCatalog() {
        store.remove(KEY_CATALOG)
        refresh()
    }

    companion object {
        const val KEY_API_KEY = "parfmi.api_key"
        const val KEY_SERVICE = "parfmi.service_id"
        const val KEY_PROVIDER = "parfmi.provider_id"
        const val KEY_CATALOG = "parfmi.service_catalog"
        const val DEFAULT_SERVICE_ID = "72"
    }
}
