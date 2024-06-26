package com.paydock.feature.card.data.api.auth

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add a public key as a header to the HTTP request.
 *
 * @param publicKey The public key to be added as a header to the request.
 */
class CardAuthInterceptor(private val publicKey: String) : Interceptor {
    /**
     * Intercepts the HTTP request and adds the public key as a header.
     *
     * @param chain The interceptor chain.
     * @return The response from the server.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the original request from the chain
        val original = chain.request()

        // Create a new request with the public key added as a header
        val request = original.newBuilder()
            .addHeader("x-user-public-key", publicKey)
            .build()

        // Proceed with the new request and return the response
        return chain.proceed(request)
    }
}