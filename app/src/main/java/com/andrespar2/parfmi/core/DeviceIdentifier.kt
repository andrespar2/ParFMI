package com.andrespar2.parfmi.core

/**
 * Identificador de dispositivo ya normalizado.
 *
 * [raw] conserva exactamente lo que escribio el usuario (para poder
 * mostrarlo tal cual) y [normalized] es la version sin separadores y en
 * mayusculas que se envia al proveedor.
 */
data class DeviceIdentifier(
    val raw: String,
    val normalized: String,
    val type: IdentifierType,
) {
    /** Representacion enmascarada, apta para logs o mensajes de error. */
    fun masked(): String = Redactor.mask(normalized)

    override fun toString(): String = "DeviceIdentifier(type=$type, value=${masked()})"
}
