package com.andrespar2.parfmi.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit

/** [KeyValueStore] sobre SharedPreferences. */
class SharedPreferencesStore(
    private val preferences: SharedPreferences,
) : KeyValueStore {

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String) {
        preferences.edit { putString(key, value) }
    }

    override fun remove(key: String) {
        preferences.edit { remove(key) }
    }

    override fun contains(key: String): Boolean = preferences.contains(key)
}

/**
 * Guarda secretos cifrados dentro de un [KeyValueStore].
 *
 * Si el cifrado falla, el valor NO se almacena en claro: se prefiere perder el
 * dato antes que escribirlo sin proteger.
 */
class EncryptedSecretStore(
    private val store: KeyValueStore,
    private val cipher: SecretCipher,
) : SecretStore {

    override fun put(key: String, value: String) {
        val encrypted = cipher.encrypt(value) ?: return
        store.putString(key, encrypted)
    }

    override fun get(key: String): String? {
        val stored = store.getString(key) ?: return null
        return cipher.decrypt(stored)
    }

    override fun remove(key: String) {
        store.remove(key)
    }

    override fun contains(key: String): Boolean = store.contains(key)
}
