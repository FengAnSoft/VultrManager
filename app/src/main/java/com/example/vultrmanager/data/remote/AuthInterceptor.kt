package com.example.vultrmanager.data.remote

import com.example.vultrmanager.data.local.ApiKeyStore
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds the Vultr API v2 authentication header to every outgoing request.
 *
 * Header spec (https://www.vultr.com/api/):
 *     Authorization: Bearer <API_KEY>
 *
 * The key is read dynamically from [ApiKeyStore] on each request so that changes
 * made in the Settings screen take effect immediately without rebuilding the client.
 */
class AuthInterceptor(private val apiKeyStore: ApiKeyStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .header("Accept", "application/json")

        apiKeyStore.getApiKey()?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
