/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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