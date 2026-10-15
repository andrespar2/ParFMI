package com.andrespar2.parfmi.core

/**
 * Tipo de identificador introducido por el usuario.
 *
 * [UNKNOWN] nunca se trata como un estado valido: si el formato no se puede
 * reconocer, la app lo indica en lugar de asumir un tipo.
 */
enum class IdentifierType {
    /** IMEI numerico de 15 digitos con digito de control Luhn valido. */
    IMEI,

    /** Numero de serie (SN) de Apple. */
    SERIAL_NUMBER,

    /** Formato no reconocido. */
    UNKNOWN,
}
