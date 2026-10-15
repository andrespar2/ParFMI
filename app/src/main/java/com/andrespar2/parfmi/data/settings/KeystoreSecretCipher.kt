package com.andrespar2.parfmi.data.settings

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Cifrado AES-GCM con una clave generada y custodiada por Android Keystore.
 *
 * La clave privada nunca sale del keystore del dispositivo y no se puede
 * exportar, por lo que la API key almacenada solo es legible en este
 * dispositivo.
 */
class KeystoreSecretCipher(
    private val alias: String = DEFAULT_ALIAS,
) : SecretCipher {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    override fun encrypt(plainText: String): String? = try {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }

    override fun decrypt(cipherText: String): String? = try {
        val payload = Base64.decode(cipherText, Base64.NO_WRAP)
        if (payload.size <= GCM_IV_LENGTH) {
            null
        } else {
            val iv = payload.copyOfRange(0, GCM_IV_LENGTH)
            val encrypted = payload.copyOfRange(GCM_IV_LENGTH, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }
    } catch (e: Exception) {
        null
    }

    private fun getOrCreateKey(): SecretKey {
        keyStore.getEntry(alias, null)?.let { entry ->
            (entry as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        const val DEFAULT_ALIAS = "parfmi_secret_key_v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_BITS = 128
        private const val KEY_SIZE_BITS = 256
    }
}
