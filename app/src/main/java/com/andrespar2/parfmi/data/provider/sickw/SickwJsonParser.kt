package com.andrespar2.parfmi.data.provider.sickw

import com.andrespar2.parfmi.data.model.ProviderError
import com.andrespar2.parfmi.data.model.ProviderService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Datos extraidos de una respuesta correcta de Sickw.
 *
 * La API documenta esta forma:
 * ```
 * {"result": {...}, "imei": "...", "balance": "...", "price": "...",
 *  "service": "...", "id": "...", "status": "success"}
 * ```
 * `result` puede ser un objeto (datos del dispositivo) o un texto. Los campos
 * del objeto se aplanan a un mapa etiqueta -> valor conservando el orden.
 */
data class SickwPayload(
    val fields: Map<String, String>,
    val textPayload: String? = null,
    val status: String? = null,
    val balance: String? = null,
    val price: String? = null,
    val service: String? = null,
    val orderId: String? = null,
    val rawResult: String? = null,
)

/** Resultado de interpretar el cuerpo de una respuesta de Sickw. */
sealed interface SickwParseResult {
    data class Data(val payload: SickwPayload) : SickwParseResult

    data class ServiceList(val services: List<ProviderService>) : SickwParseResult

    /** El proveedor respondio con un mensaje de texto (error o aviso). */
    data class Message(val message: String) : SickwParseResult

    data class Malformed(val reason: String) : SickwParseResult
}

/**
 * Parser puro de las respuestas de Sickw.
 *
 * No realiza peticiones de red, por lo que se puede probar con ejemplos reales
 * de la documentacion.
 */
object SickwJsonParser {

    private const val KEY_SERVICES = "Service List"
    private const val KEY_RESULT = "result"
    private const val KEY_STATUS = "status"
    private const val KEY_ERROR = "error"

    private const val STATUS_SUCCESS = "success"

    fun parse(body: String): SickwParseResult {
        if (body.isBlank()) return SickwParseResult.Malformed("respuesta vacia")

        val root = try {
            Json.parseToJsonElement(body)
        } catch (e: Exception) {
            return SickwParseResult.Malformed("JSON no valido")
        }

        val rootObject = root as? JsonObject
            ?: return SickwParseResult.Malformed("la respuesta no es un objeto JSON")

        // Lista de servicios (cuando se omite el identificador a consultar).
        rootObject[KEY_SERVICES]?.let { element ->
            return SickwParseResult.ServiceList(parseServiceList(element))
        }

        val status = rootObject.stringOrNull(KEY_STATUS)
        val resultElement = rootObject[KEY_RESULT]

        // Mensaje de error explicito.
        rootObject.stringOrNull(KEY_ERROR)?.let { error ->
            return SickwParseResult.Message(error)
        }

        if (resultElement == null) {
            return if (status != null && status.equals(STATUS_SUCCESS, ignoreCase = true)) {
                SickwParseResult.Malformed("la respuesta no incluye el campo result")
            } else {
                SickwParseResult.Message(status ?: "respuesta sin datos")
            }
        }

        if (resultElement is JsonPrimitive) {
            val text = resultElement.contentOrNull.orEmpty()
            return if (status.equals(STATUS_SUCCESS, ignoreCase = true)) {
                SickwParseResult.Data(
                    SickwPayload(
                        fields = emptyMap(),
                        textPayload = text,
                        status = status,
                        balance = rootObject.stringOrNull("balance"),
                        price = rootObject.stringOrNull("price"),
                        service = rootObject.stringOrNull("service"),
                        orderId = rootObject.stringOrNull("id"),
                    )
                )
            } else {
                SickwParseResult.Message(text.ifBlank { status ?: "sin detalle" })
            }
        }

        // Respuesta correcta con datos estructurados.
        val fields = linkedMapOf<String, String>()
        flatten(resultElement, prefix = "", out = fields)

        return SickwParseResult.Data(
            SickwPayload(
                fields = fields,
                status = status,
                balance = rootObject.stringOrNull("balance"),
                price = rootObject.stringOrNull("price"),
                service = rootObject.stringOrNull("service"),
                orderId = rootObject.stringOrNull("id"),
                rawResult = resultElement.toString(),
            )
        )
    }

    /** Aplana objetos y arrays anidados en pares etiqueta -> valor. */
    private fun flatten(element: JsonElement, prefix: String, out: MutableMap<String, String>) {
        when (element) {
            is JsonObject -> element.forEach { (key, value) ->
                val label = if (prefix.isEmpty()) key else "$prefix.$key"
                flatten(value, label, out)
            }

            is JsonArray -> element.forEachIndexed { index, value ->
                val label = if (prefix.isEmpty()) "Item ${index + 1}" else "$prefix ${index + 1}"
                flatten(value, label, out)
            }

            is JsonPrimitive -> {
                val content = element.contentOrNull
                if (content != null && prefix.isNotEmpty()) out[prefix] = content
            }

            else -> Unit
        }
    }

    private fun parseServiceList(element: JsonElement): List<ProviderService> {
        val array = element as? JsonArray ?: return emptyList()
        return array.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val id = obj.stringOrNull("service") ?: obj.stringOrNull("id") ?: return@mapNotNull null
            ProviderService(
                id = id,
                name = obj.stringOrNull("name") ?: "Servicio $id",
                price = obj.stringOrNull("price"),
                defaultService = id == SickwServiceCatalog.DEFAULT_SERVICE_ID,
            )
        }
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
}

/**
 * Traduce los mensajes de texto de Sickw a categorias de error conocidas.
 * Se basa en las palabras que usa el proveedor ("Invalid API key",
 * "Insufficient balance", "not found", "limit").
 */
object SickwMessageClassifier {

    fun classify(message: String): ProviderError {
        val text = message.lowercase()
        return when {
            text.contains("invalid api") || text.contains("api key") || text.contains("unauthorized") ->
                ProviderError.UNAUTHORIZED

            text.contains("balance") || text.contains("credit") || text.contains("insufficient") ->
                ProviderError.INSUFFICIENT_BALANCE

            text.contains("not found") || text.contains("no data") || text.contains("no result") ||
                text.contains("not exist") || text.contains("unknown device") ->
                ProviderError.NOT_FOUND

            text.contains("limit") || text.contains("too many") || text.contains("flood") ->
                ProviderError.RATE_LIMITED

            text.contains("timeout") -> ProviderError.TIMEOUT

            else -> ProviderError.REQUEST_REJECTED
        }
    }
}
