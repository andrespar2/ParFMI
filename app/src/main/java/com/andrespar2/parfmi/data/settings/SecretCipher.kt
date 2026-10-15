package com.andrespar2.parfmi.data.settings

/**
 * Cifrado simetrico para los secretos.
 *
 * Se abstrae para poder probar el almacenamiento sin Android Keystore.
 */
interface SecretCipher {
    /** Devuelve `null` si el cifrado no esta disponible. */
    fun encrypt(plainText: String): String?

    /** Devuelve `null` si el texto no se puede descifrar. */
    fun decrypt(cipherText: String): String?
}

/**
 * Cifrado en memoria de uso exclusivo en pruebas (NO es seguro, no se usa en
 * la aplicacion real).
 */
class IdentitySecretCipher : SecretCipher {
    override fun encrypt(plainText: String): String = "enc:$plainText"

    override fun decrypt(cipherText: String): String? =
        cipherText.removePrefix("enc:").takeIf { cipherText.startsWith("enc:") }
}
