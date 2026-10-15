package com.andrespar2.parfmi.core

/**
 * Validacion de numeros de serie de Apple.
 *
 * Apple ha usado varios formatos a lo largo de los anios (11 o 12 caracteres
 * alfanumericos en equipos antiguos, cadenas mas largas en modelos recientes
 * y series completamente numericas en algunos productos), por lo que aqui solo
 * se aplican comprobaciones razonables: longitud y juego de caracteres.
 * Deliberadamente no se impone un patron de formato cerrado para no rechazar
 * numeros de serie legitimos.
 */
object SerialNumberValidator {

    const val MIN_LENGTH = 8
    const val MAX_LENGTH = 20

    private val ALLOWED_CHARACTERS = Regex("^[A-Z0-9]+$")

    fun hasValidLength(value: String): Boolean = value.length in MIN_LENGTH..MAX_LENGTH

    fun hasValidCharacters(value: String): Boolean = ALLOWED_CHARACTERS.matches(value)

    /** Longitud y caracteres dentro de rangos razonables. */
    fun isPlausible(value: String): Boolean =
        hasValidLength(value) && hasValidCharacters(value)
}
