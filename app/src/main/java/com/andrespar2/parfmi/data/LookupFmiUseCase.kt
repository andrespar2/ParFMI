package com.andrespar2.parfmi.data

import com.andrespar2.parfmi.core.IdentifierValidation
import com.andrespar2.parfmi.core.IdentifierValidator
import com.andrespar2.parfmi.core.ValidationError
import com.andrespar2.parfmi.data.model.LookupOutcome
import com.andrespar2.parfmi.data.model.ProviderError
import com.andrespar2.parfmi.data.model.ServiceCatalogResult
import com.andrespar2.parfmi.data.net.ConnectivityChecker
import com.andrespar2.parfmi.data.provider.FmiProvider
import com.andrespar2.parfmi.data.provider.LookupRequest
import com.andrespar2.parfmi.data.settings.SettingsRepository

/**
 * Caso de uso principal: validar la entrada, comprobar la configuracion y
 * delegar la consulta en el proveedor activo.
 *
 * Devuelve siempre un resultado tipificado; nunca inventa datos ni lanza
 * excepciones hacia la interfaz.
 */
class LookupFmiUseCase(
    private val provider: FmiProvider,
    private val settingsRepository: SettingsRepository,
    private val connectivityChecker: ConnectivityChecker,
) {

    /** Resultado de la fase de validacion previa a la consulta. */
    sealed interface PreCheck {
        data class Ready(val request: LookupRequest) : PreCheck
        data class Rejected(val outcome: LookupOutcome.Failure) : PreCheck
        data class InvalidInput(val error: ValidationError) : PreCheck
    }

    fun preCheck(rawInput: String): PreCheck {
        val validation = IdentifierValidator.validate(rawInput)
        if (validation is IdentifierValidation.Invalid) {
            return PreCheck.InvalidInput(validation.error)
        }

        val identifier = (validation as IdentifierValidation.Valid).identifier
        val settings = settingsRepository.settings.value

        val apiKey = settingsRepository.apiKey()
        if (apiKey.isNullOrBlank()) {
            return PreCheck.Rejected(
                LookupOutcome.Failure(ProviderError.NO_PROVIDER_CONFIGURED)
            )
        }

        if (!connectivityChecker.isOnline()) {
            return PreCheck.Rejected(LookupOutcome.Failure(ProviderError.NO_NETWORK))
        }

        return PreCheck.Ready(
            LookupRequest(
                identifier = identifier,
                serviceId = settings.serviceId,
                serviceName = settings.selectedServiceName,
                apiKey = apiKey,
            )
        )
    }

    suspend fun execute(rawInput: String): LookupOutcome = when (val check = preCheck(rawInput)) {
        is PreCheck.Rejected -> check.outcome
        is PreCheck.InvalidInput -> LookupOutcome.Failure(
            ProviderError.REQUEST_REJECTED,
            "validacion: ${check.error.name}",
        )

        is PreCheck.Ready -> provider.lookup(check.request)
    }

    suspend fun refreshCatalog(): ServiceCatalogResult {
        val apiKey = settingsRepository.apiKey()
            ?: return ServiceCatalogResult.Failure(ProviderError.NO_PROVIDER_CONFIGURED)

        if (!connectivityChecker.isOnline()) {
            return ServiceCatalogResult.Failure(ProviderError.NO_NETWORK)
        }

        val result = provider.fetchServices(apiKey)
        if (result is ServiceCatalogResult.Success) {
            settingsRepository.saveCatalog(result.services)
        }
        return result
    }
}
