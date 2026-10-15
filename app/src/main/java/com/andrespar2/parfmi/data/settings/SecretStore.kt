package com.andrespar2.parfmi.data.settings

/**
 * Almacen de secretos (API key del proveedor).
 *
 * El contrato es deliberadamente simple para poder sustituir la
 * implementacion por una en memoria durante las pruebas.
 */
interface SecretStore {
    fun put(key: String, value: String)
    fun get(key: String): String?
    fun remove(key: String)
    fun contains(key: String): Boolean
}

/** Implementacion en memoria para pruebas. */
class InMemorySecretStore : SecretStore {
    private val values = mutableMapOf<String, String>()

    override fun put(key: String, value: String) {
        values[key] = value
    }

    override fun get(key: String): String? = values[key]

    override fun remove(key: String) {
        values.remove(key)
    }

    override fun contains(key: String): Boolean = values.containsKey(key)
}
