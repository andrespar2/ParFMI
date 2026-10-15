package com.andrespar2.parfmi.core

/** Motivos por los que una entrada no se acepta. */
enum class ValidationError {
    EMPTY,
    TOO_LONG,
    IMEI_LENGTH,
    IMEI_CHECKSUM,
    SERIAL_LENGTH,
    SERIAL_CHARACTERS,
}

/** Resultado de validar lo que ha escrito el usuario. */
sealed interface IdentifierValidation {
    data class Valid(val identifier: DeviceIdentifier) : IdentifierValidation
    data class Invalid(val error: ValidationError) : IdentifierValidation
}
