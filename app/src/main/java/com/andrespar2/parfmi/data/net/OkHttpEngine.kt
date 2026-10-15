package com.andrespar2.parfmi.data.net

import com.andrespar2.parfmi.data.model.ProviderError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Implementacion de [HttpEngine] sobre OkHttp.
 *
 * Solo se realizan peticiones GET por HTTPS al proveedor configurado.
 */
class OkHttpEngine(
    private val client: OkHttpClient = HttpClientFactory.create(),
) : HttpEngine {

    override suspend fun get(url: String, headers: Map<String, String>): HttpResult =
        withContext(Dispatchers.IO) {
            val request = try {
                val headerBuilder = Headers.Builder()
                headers.forEach { (name, value) -> headerBuilder.add(name, value) }
                Request.Builder()
                    .url(url)
                    .headers(headerBuilder.build())
                    .get()
                    .build()
            } catch (e: IllegalArgumentException) {
                return@withContext HttpResult.NetworkFailure(
                    ProviderError.UNKNOWN,
                    "URL no valida",
                )
            }

            try {
                client.newCall(request).execute().use { response ->
                    HttpResult.Response(
                        code = response.code,
                        body = response.body?.string().orEmpty(),
                    )
                }
            } catch (e: IOException) {
                HttpResult.NetworkFailure(
                    error = NetworkErrorMapper.fromException(e),
                    detail = e.javaClass.simpleName,
                )
            }
        }
}
