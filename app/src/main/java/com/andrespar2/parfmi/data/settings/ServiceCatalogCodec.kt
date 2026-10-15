package com.andrespar2.parfmi.data.settings

import com.andrespar2.parfmi.data.model.ProviderService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Serializa el catalogo de servicios para guardarlo en preferencias.
 *
 * No se guarda informacion sensible: solo IDs, nombres y precios publicados.
 */
object ServiceCatalogCodec {

    fun encode(services: List<ProviderService>): String {
        val array = buildJsonArray {
            services.forEach { service ->
                add(
                    buildJsonObject {
                        put("id", service.id)
                        put("name", service.name)
                        service.price?.let { put("price", it) }
                        put("default", service.defaultService)
                    }
                )
            }
        }
        return array.toString()
    }

    fun decode(json: String?): List<ProviderService> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            Json.parseToJsonElement(json).jsonArray.mapNotNull { element ->
                val obj = element.jsonObject
                val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                ProviderService(
                    id = id,
                    name = obj["name"]?.jsonPrimitive?.contentOrNull ?: id,
                    price = obj["price"]?.jsonPrimitive?.contentOrNull,
                    defaultService = obj["default"]?.jsonPrimitive?.contentOrNull == "true",
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
