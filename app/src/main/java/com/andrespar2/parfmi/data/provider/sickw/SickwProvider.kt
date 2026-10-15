package com.andrespar2.parfmi.data.provider.sickw

import com.andrespar2.parfmi.core.TimeSource
import com.andrespar2.parfmi.data.model.LookupOutcome
import com.andrespar2.parfmi.data.model.ProviderError
import com.andrespar2.parfmi.data.model.ServiceCatalogResult
import com.andrespar2.parfmi.data.net.HttpEngine
import com.andrespar2.parfmi.data.net.HttpResult
import com.andrespar2.parfmi.data.net.NetworkErrorMapper
import com.andrespar2.parfmi.data.provider.FmiProvider
import com.andrespar2.parfmi.data.provider.LookupRequest
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Proveedor de consultas basado en la API publica de Sickw.com.
 *
 * Endpoint documentado por el proveedor:
 * `https://sickw.com/api.php?format=json&key=API_KEY&imei=IMEI_O_SN&service=ID`
 *
 * La API key la aporta el usuario en Ajustes y se guarda cifrada. Sin clave,
 * el proveedor no se usa: la app lo indica en lugar de mostrar datos falsos.
 */
class SickwProvider(
    private val http: HttpEngine,
    private val timeSource: TimeSource = TimeSource.System,
    private val baseUrl: String = SickwServiceCatalog.BASE_URL,
) : FmiProvider {

    override val id: String = SickwServiceCatalog.PROVIDER_ID
    override val displayName: String = SickwServiceCatalog.PROVIDER_NAME

    private val mapper = SickwFieldMapper()

    override suspend fun lookup(request: LookupRequest): LookupOutcome {
        val url = buildLookupUrl(
            identifier = request.identifier.normalized,
            serviceId = request.serviceId,
            apiKey = request.apiKey,
            format = FORMAT_JSON,
        ) ?: return LookupOutcome.Failure(ProviderError.UNKNOWN, "no se pudo construir la URL")

        return when (val result = http.get(url)) {
            is HttpResult.NetworkFailure -> LookupOutcome.Failure(result.error, result.detail)

            is HttpResult.Response -> handleResponse(
                response = result,
                request = request,
            )
        }
    }

    override suspend fun fetchServices(apiKey: String): ServiceCatalogResult {
        // El proveedor devuelve la lista de servicios cuando no se indica
        // identificador. Se usa como actualizacion opcional del catalogo.
        val url = buildServicesUrl(apiKey) 
            ?: return ServiceCatalogResult.Failure(
                ProviderError.UNKNOWN,
                "no se pudo construir la URL",
            )

        return when (val result = http.get(url)) {
            is HttpResult.NetworkFailure -> ServiceCatalogResult.Failure(result.error, result.detail)

            is HttpResult.Response -> {
                if (result.code != HTTP_OK) {
                    return ServiceCatalogResult.Failure(
                        NetworkErrorMapper.fromStatusCode(result.code),
                        "HTTP ${result.code}",
                    )
                }
                when (val parsed = SickwJsonParser.parse(result.body)) {
                    is SickwParseResult.ServiceList -> {
                        if (parsed.services.isEmpty()) {
                            ServiceCatalogResult.Failure(
                                ProviderError.PARSE_ERROR,
                                "el catalogo devuelto esta vacio",
                            )
                        } else {
                            ServiceCatalogResult.Success(parsed.services)
                        }
                    }

                    is SickwParseResult.Message -> ServiceCatalogResult.Failure(
                        SickwMessageClassifier.classify(parsed.message),
                        parsed.message,
                    )

                    is SickwParseResult.Malformed -> ServiceCatalogResult.Failure(
                        ProviderError.PARSE_ERROR,
                        parsed.reason,
                    )

                    is SickwParseResult.Data -> ServiceCatalogResult.Failure(
                        ProviderError.PARSE_ERROR,
                        "el proveedor no devolvio una lista de servicios",
                    )
                }
            }
        }
    }

    private fun handleResponse(
        response: HttpResult.Response,
        request: LookupRequest,
    ): LookupOutcome {
        if (response.code != HTTP_OK) {
            return LookupOutcome.Failure(
                NetworkErrorMapper.fromStatusCode(response.code),
                "HTTP ${response.code}",
            )
        }

        return when (val parsed = SickwJsonParser.parse(response.body)) {
            is SickwParseResult.Data -> LookupOutcome.Success(
                mapper.toReport(
                    payload = parsed.payload,
                    identifier = request.identifier,
                    serviceId = request.serviceId,
                    serviceName = request.serviceName,
                    queriedAtMillis = timeSource.nowMillis(),
                    rawResponse = response.body,
                )
            )

            is SickwParseResult.Message -> LookupOutcome.Failure(
                SickwMessageClassifier.classify(parsed.message),
                parsed.message,
            )

            is SickwParseResult.Malformed -> LookupOutcome.Failure(
                ProviderError.PARSE_ERROR,
                parsed.reason,
            )

            is SickwParseResult.ServiceList -> LookupOutcome.Failure(
                ProviderError.PARSE_ERROR,
                "el proveedor devolvio un catalogo en lugar de un resultado",
            )
        }
    }

    /** Construye la URL de consulta con los parametros documentados. */
    internal fun buildLookupUrl(
        identifier: String,
        serviceId: String,
        apiKey: String,
        format: String,
    ): String? = baseUrl.toHttpUrlOrNull()
        ?.newBuilder()
        ?.addQueryParameter("format", format)
        ?.addQueryParameter("key", apiKey)
        ?.addQueryParameter("imei", identifier)
        ?.addQueryParameter("service", serviceId)
        ?.build()
        ?.toString()

    /** URL para pedir la lista de servicios (sin identificador). */
    internal fun buildServicesUrl(apiKey: String, format: String = FORMAT_JSON): String? =
        baseUrl.toHttpUrlOrNull()
            ?.newBuilder()
            ?.addQueryParameter("format", format)
            ?.addQueryParameter("key", apiKey)
            ?.build()
            ?.toString()

    companion object {
        const val FORMAT_JSON = "json"
        private const val HTTP_OK = 200
    }
}
