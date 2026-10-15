package com.andrespar2.parfmi.di

import android.content.Context
import com.andrespar2.parfmi.data.LookupFmiUseCase
import com.andrespar2.parfmi.data.net.AndroidConnectivityChecker
import com.andrespar2.parfmi.data.net.ConnectivityChecker
import com.andrespar2.parfmi.data.net.HttpClientFactory
import com.andrespar2.parfmi.data.net.OkHttpEngine
import com.andrespar2.parfmi.data.provider.FmiProvider
import com.andrespar2.parfmi.data.provider.sickw.SickwProvider
import com.andrespar2.parfmi.data.provider.sickw.SickwServiceCatalog
import com.andrespar2.parfmi.data.settings.EncryptedSecretStore
import com.andrespar2.parfmi.data.settings.KeyValueStore
import com.andrespar2.parfmi.data.settings.KeystoreSecretCipher
import com.andrespar2.parfmi.data.settings.SecretStore
import com.andrespar2.parfmi.data.settings.SettingsRepository
import com.andrespar2.parfmi.data.settings.SharedPreferencesStore

/**
 * Contenedor de dependencias manual (sin framework de inyeccion).
 *
 * Se crea una sola vez en [com.andrespar2.parfmi.ParFmiApplication] y expone
 * las piezas ya conectadas. Al ser explicito, se ve de un vistazo que la API
 * key se guarda cifrada y que el unico proveedor es Sickw.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val keyValueStore: KeyValueStore = SharedPreferencesStore(
        appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
    )

    private val secretStore: SecretStore = EncryptedSecretStore(
        store = keyValueStore,
        cipher = KeystoreSecretCipher(),
    )

    private val connectivityChecker: ConnectivityChecker = AndroidConnectivityChecker(appContext)

    val settingsRepository: SettingsRepository = SettingsRepository(
        store = keyValueStore,
        secretStore = secretStore,
        defaultServices = SickwServiceCatalog.services,
        defaultProviderId = SickwServiceCatalog.PROVIDER_ID,
    )

    private val provider: FmiProvider = SickwProvider(
        http = OkHttpEngine(HttpClientFactory.create()),
    )

    val lookupFmiUseCase: LookupFmiUseCase = LookupFmiUseCase(
        provider = provider,
        settingsRepository = settingsRepository,
        connectivityChecker = connectivityChecker,
    )

    companion object {
        private const val PREFERENCES_NAME = "parfmi_settings"
    }
}
