package com.andrespar2.parfmi.data.settings

/** Almacen de clave/valor para preferencias no sensibles. */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun contains(key: String): Boolean
}

/** Implementacion en memoria, usada en pruebas. */
class InMemoryKeyValueStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String): String? = values[key]

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }

    override fun contains(key: String): Boolean = values.containsKey(key)
}
