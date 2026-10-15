package com.andrespar2.parfmi.core

import java.util.Locale

/**
 * Normalizacion de texto para comparar valores devueltos por proveedores.
 *
 * Los proveedores no son consistentes con mayusculas, espacios ni signos de
 * puntuacion ("Out Of Warranty (No Coverage)" frente a "OUT OF WARRANTY"),
 * por lo que antes de comparar se aplica un formato comun.
 */
object TextNormalizer {

    private val NOT_ALPHANUMERIC_OR_SPACE = Regex("[^A-Z0-9 ]")
    private val MULTIPLE_SPACES = Regex(" +")

    /** Devuelve `null` si el valor no aporta informacion. */
    fun normalizeOrNull(raw: String?): String? {
        val normalized = normalize(raw)
        return normalized.ifEmpty { null }
    }

    /** Version normalizada; cadena vacia si no hay texto util. */
    fun normalize(raw: String?): String {
        if (raw == null) return ""
        return raw
            .uppercase(Locale.ROOT)
            .replace(NOT_ALPHANUMERIC_OR_SPACE, " ")
            .replace(MULTIPLE_SPACES, " ")
            .trim()
    }

    /** Los proveedores usan estos textos para indicar "sin dato". */
    fun isMissingValue(raw: String?): Boolean {
        val normalized = normalize(raw)
        return normalized.isEmpty() || normalized in MISSING_VALUES
    }

    /** Limpia un valor para mostrarlo, conservando el texto original. */
    fun displayOrNull(raw: String?): String? {
        val trimmed = raw?.trim().orEmpty()
        return if (trimmed.isEmpty() || trimmed in MISSING_VALUES_RAW) null else trimmed
    }

    private val MISSING_VALUES = setOf(
        "N A", "NA", "N/A", "NULL", "NONE", "UNKNOWN", "NOT AVAILABLE",
        "NO DATA", "NO INFORMATION", "-", "--", "0", "UNDEFINED",
    )

    private val MISSING_VALUES_RAW = setOf("n/a", "N/A", "NA", "null", "-", "--", "unknown", "Unknown")
}
