package com.andrespar2.parfmi.core

import java.util.Locale

/**
 * Punto de entrada unico para interpretar un IMEI o un numero de serie.
 *
 * Reglas aplicadas:
 *  - Se ignoran espacios, guiones y cualquier separador no alfanumerico.
 *  - Un valor con 15 digitos se interpreta como IMEI y se valida su digito de
 *    control. Si el control falla, se informa del error (no se consulta).
 *  - Un valor numerico de 14 o 16 digitos se considera un IMEI mal escrito.
 *  - Cualquier otro valor alfanumerico entre 8 y 20 caracteres se trata como
 *    numero de serie.
 */
object IdentifierValidator {

    /** Limite defensivo para evitar entradas desmesuradas. */
    const val MAX_INPUT_LENGTH = 40

    private val SEPARATORS = Regex("[^A-Za-z0-9]")

    fun normalize(input: String): String =
        input.replace(SEPARATORS, "").uppercase(Locale.ROOT)

    fun validate(input: String): IdentifierValidation {
        val normalized = normalize(input)

        if (normalized.isEmpty()) return IdentifierValidation.Invalid(ValidationError.EMPTY)
        if (normalized.length > MAX_INPUT_LENGTH) {
            return IdentifierValidation.Invalid(ValidationError.TOO_LONG)
        }

        return if (normalized.all { it.isDigit() }) {
            validateNumeric(normalized, input)
        } else {
            validateSerialNumber(normalized, input)
        }
    }

    /** Tipo detectado, o [IdentifierType.UNKNOWN] si el valor no es valido. */
    fun detectType(input: String): IdentifierType =
        when (val result = validate(input)) {
            is IdentifierValidation.Valid -> result.identifier.type
            is IdentifierValidation.Invalid -> IdentifierType.UNKNOWN
        }

    private fun validateNumeric(normalized: String, raw: String): IdentifierValidation = when {
        normalized.length == ImeiValidator.LENGTH -> {
            if (ImeiValidator.isValidChecksum(normalized)) {
                IdentifierValidation.Valid(
                    DeviceIdentifier(raw = raw, normalized = normalized, type = IdentifierType.IMEI)
                )
            } else {
                IdentifierValidation.Invalid(ValidationError.IMEI_CHECKSUM)
            }
        }

        ImeiValidator.isLikelyMistypedImei(normalized) ->
            IdentifierValidation.Invalid(ValidationError.IMEI_LENGTH)

        SerialNumberValidator.isPlausible(normalized) ->
            IdentifierValidation.Valid(
                DeviceIdentifier(
                    raw = raw,
                    normalized = normalized,
                    type = IdentifierType.SERIAL_NUMBER,
                )
            )

        else -> IdentifierValidation.Invalid(ValidationError.SERIAL_LENGTH)
    }

    private fun validateSerialNumber(normalized: String, raw: String): IdentifierValidation = when {
        !SerialNumberValidator.hasValidLength(normalized) ->
            IdentifierValidation.Invalid(ValidationError.SERIAL_LENGTH)

        !SerialNumberValidator.hasValidCharacters(normalized) ->
            IdentifierValidation.Invalid(ValidationError.SERIAL_CHARACTERS)

        else -> IdentifierValidation.Valid(
            DeviceIdentifier(
                raw = raw,
                normalized = normalized,
                type = IdentifierType.SERIAL_NUMBER,
            )
        )
    }
}
