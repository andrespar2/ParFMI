package com.andrespar2.parfmi.data.net

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP compartido.
 *
 * No se instala ningun interceptor de registro: las URL de consulta incluyen
 * la API key y el IMEI, por lo que nunca deben escribirse en el log.
 */
object HttpClientFactory {

    const val TIMEOUT_SECONDS = 25L

    fun create(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(TIMEOUT_SECONDS * 2, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
}
