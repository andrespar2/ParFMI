package com.andrespar2.parfmi.ui.lookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrespar2.parfmi.core.IdentifierValidation
import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.core.IdentifierValidator
import com.andrespar2.parfmi.core.ValidationError
import com.andrespar2.parfmi.data.LookupFmiUseCase
import com.andrespar2.parfmi.data.model.ServiceCatalogResult
import com.andrespar2.parfmi.data.settings.AppSettings
import com.andrespar2.parfmi.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado y logica de la pantalla de consulta.
 *
 * Mantiene el texto introducido, el servicio elegido y el resultado de la
 * ultima consulta. La validacion de formato se ofrece en vivo (tipo detectado)
 * pero el mensaje de error solo aparece tras pulsar "Consultar", para no
 * molestar mientras se escribe.
 */
class LookupViewModel(
    private val lookupFmi: LookupFmiUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _inputError = MutableStateFlow<ValidationError?>(null)
    val inputError: StateFlow<ValidationError?> = _inputError.asStateFlow()

    private val _status = MutableStateFlow<LookupStatus>(LookupStatus.Idle)
    val status: StateFlow<LookupStatus> = _status.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    /** Tipo de identificador detectado en vivo, o `null` si aun no es valido. */
    fun detectedType(): IdentifierType? =
        (IdentifierValidator.validate(_input.value) as? IdentifierValidation.Valid)
            ?.identifier
            ?.type

    fun onInputChange(value: String) {
        _input.value = value
        _inputError.value = null
    }

    fun clearInput() {
        _input.value = ""
        _inputError.value = null
    }

    /**
     * Lanza la consulta. [onNavigateToResult] se invoca cuando hay que mostrar
     * la pantalla de resultados (consulta en curso, fallo previo o exito). Si
     * la entrada no es valida no se navega: se marca [inputError].
     */
    fun submit(onNavigateToResult: () -> Unit) {
        if (_status.value is LookupStatus.Loading) return

        when (val check = lookupFmi.preCheck(_input.value)) {
            is LookupFmiUseCase.PreCheck.InvalidInput -> {
                _inputError.value = check.error
            }

            is LookupFmiUseCase.PreCheck.Rejected -> {
                _status.value = LookupStatus.Done(check.outcome)
                onNavigateToResult()
            }

            is LookupFmiUseCase.PreCheck.Ready -> {
                val serviceLabel = check.request.serviceName ?: check.request.serviceId
                _status.value = LookupStatus.Loading(serviceLabel)
                onNavigateToResult()
                viewModelScope.launch {
                    _status.value = LookupStatus.Done(lookupFmi.execute(_input.value))
                }
            }
        }
    }

    /** Vuelve al formulario limpiando el resultado anterior. */
    fun resetResult() {
        _status.value = LookupStatus.Idle
    }

    /** Actualiza el catalogo de servicios desde el proveedor. */
    fun refreshCatalog(onFinished: (ServiceCatalogResult) -> Unit = {}) {
        viewModelScope.launch {
            onFinished(lookupFmi.refreshCatalog())
        }
    }
}
