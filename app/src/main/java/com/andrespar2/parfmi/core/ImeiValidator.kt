package com.andrespar2.parfmi.core

/**
 * Validacion de IMEI: longitud y digito de control (algoritmo de Luhn).
 *
 * El IMEI estandar tiene 15 digitos y el ultimo es el digito de control.
 * Se aplica Luhn sobre la totalidad del numero, empezando a duplicar por el
 * penultimo digito.
 */
object ImeiValidator {

    /** Longitud del IMEI estandar (14 digitos + digito de control). */
    const val LENGTH = 15

    /** Longitudes que, siendo numericas, suelen indicar un IMEI mal escrito. */
    private val LIKELY_MISTYPED_IMEI_LENGTHS = setOf(14, 16)

    fun isValidLength(value: String): Boolean = value.length == LENGTH

    /** Indica si la longitud es la de un IMEI escrito de forma incompleta. */
    fun isLikelyMistypedImei(value: String): Boolean =
        value.all { it.isDigit() } && value.length in LIKELY_MISTYPED_IMEI_LENGTHS

    /**
     * Comprueba el digito de control de Luhn.
     *
     * @return `false` si el valor esta vacio o contiene algo distinto a digitos.
     */
    fun isValidChecksum(value: String): Boolean = luhnCheck(value)

    /** Longitud correcta y digito de control correcto. */
    fun isComplete(value: String): Boolean =
        isValidLength(value) && isValidChecksum(value)

    /**
     * Implementacion generica de Luhn (modulo 10).
     *
     * Se recorre el numero de derecha a izquierda duplicando un digito si y
     * solo si el anterior no se ha duplicado, empezando por el penultimo.
     */
    fun luhnCheck(number: String): Boolean {
        if (number.isEmpty()) return false
        var sum = 0
        var doubleNext = false
        for (index in number.indices.reversed()) {
            val digit = number[index]
            if (!digit.isDigit()) return false
            var value = digit - '0'
            if (doubleNext) {
                value *= 2
                if (value > 9) value -= 9
            }
            sum += value
            doubleNext = !doubleNext
        }
        return sum % 10 == 0
    }
}
