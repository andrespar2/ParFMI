package com.andrespar2.parfmi.core

/**
 * Enmascarado de datos sensibles.
 *
 * Los IMEI, numeros de serie y API keys no deben aparecer completos en logs,
 * mensajes de error ni reportes. Todo lo que se registre debe pasar antes por
 * estas funciones.
 */
object Redactor {

    private const val KEEP_PREFIX = 4
    private const val KEEP_SUFFIX = 2

    /** Parametros de URL cuyo valor nunca debe registrarse. */
    private val SENSITIVE_PARAMETERS = setOf(
        "key",
        "api_key",
        "apikey",
        "token",
        "secret",
        "password",
        "imei",
        "sn",
        "serial",
        "serialnumber",
    )

    /**
     * Conserva el principio y el final del valor y oculta el resto.
     *
     * Los valores muy cortos se ocultan por completo para no revelar nada.
     */
    fun mask(value: String, keepPrefix: Int = KEEP_PREFIX, keepSuffix: Int = KEEP_SUFFIX): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return ""
        if (trimmed.length <= keepPrefix + keepSuffix) return "*".repeat(trimmed.length)
        return trimmed.take(keepPrefix) +
            "*".repeat(trimmed.length - keepPrefix - keepSuffix) +
            trimmed.takeLast(keepSuffix)
    }

    /** Sustituye por `***` el valor de los parametros sensibles de una URL. */
    fun redactUrl(url: String): String {
        val queryStart = url.indexOf('?')
        if (queryStart < 0) return url

        val base = url.substring(0, queryStart)
        val query = url.substring(queryStart + 1)

        val redactedQuery = query.split('&').joinToString("&") { parameter ->
            val separator = parameter.indexOf('=')
            if (separator < 0) {
                parameter
            } else {
                val name = parameter.substring(0, separator)
                if (SENSITIVE_PARAMETERS.contains(name.lowercase())) "$name=***" else parameter
            }
        }

        return "$base?$redactedQuery"
    }

    /** Oculta el valor de una cabecera sensible (por ejemplo `Authorization`). */
    fun redactHeader(name: String, value: String): String =
        if (isSensitiveHeader(name)) "$name: ***" else "$name: $value"

    private fun isSensitiveHeader(name: String): Boolean {
        val normalized = name.lowercase()
        return normalized.contains("authorization") ||
            normalized.contains("key") ||
            normalized.contains("token")
    }
}
