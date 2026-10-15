package com.andrespar2.parfmi.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrespar2.parfmi.data.LookupFmiUseCase
import com.andrespar2.parfmi.data.model.ServiceCatalogResult
import com.andrespar2.parfmi.data.settings.AppSettings
import com.andrespar2.parfmi.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado y logica de la pantalla de ajustes: API key, servicio elegido y
 * actualizacion opcional del catalogo.
 *
 * La API key se entrega al repositorio, que la cifra; este ViewModel nunca la
 * vuelve a leer ni la muestra una vez guardada.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val lookupFmi: LookupFmiUseCase,
) : ViewModel() {

    private val _apiKeyInput = MutableStateFlow("")
    val apiKeyInput: StateFlow<String> = _apiKeyInput.asStateFlow()

    private val _customServiceId = MutableStateFlow("")
    val customServiceId: StateFlow<String> = _customServiceId.asStateFlow()

    private val _message = MutableStateFlow<SettingsMessage?>(null)
    val message: StateFlow<SettingsMessage?> = _message.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    fun onApiKeyChange(value: String) {
        _apiKeyInput.value = value
    }

    fun saveApiKey() {
        val value = _apiKeyInput.value.trim()
        if (value.isEmpty()) return
        settingsRepository.saveApiKey(value)
        _apiKeyInput.value = ""
        _message.value = SettingsMessage.Saved
    }

    fun clearApiKey() {
        settingsRepository.clearApiKey()
        _apiKeyInput.value = ""
        _message.value = SettingsMessage.KeyCleared
    }

    fun selectService(serviceId: String) {
        settingsRepository.saveServiceId(serviceId)
        _message.value = SettingsMessage.Saved
    }

    fun onCustomServiceChange(value: String) {
        _customServiceId.value = value.filter { it.isDigit() }.take(6)
    }

    fun saveCustomService() {
        val value = _customServiceId.value.trim()
        if (value.isEmpty()) return
        settingsRepository.saveServiceId(value)
        _customServiceId.value = ""
        _message.value = SettingsMessage.Saved
    }

    fun refreshCatalog() {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            _message.value = when (val result = lookupFmi.refreshCatalog()) {
                is ServiceCatalogResult.Success -> SettingsMessage.CatalogUpdated(result.services.size)
                is ServiceCatalogResult.Failure -> SettingsMessage.CatalogFailed
            }
            _refreshing.value = false
        }
    }

    fun resetCatalog() {
        settingsRepository.resetCatalog()
        _message.value = SettingsMessage.CatalogReset
    }

    fun consumeMessage() {
        _message.value = null
    }
}
