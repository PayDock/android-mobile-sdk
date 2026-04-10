package com.paydock.feature.googlepay.data.repository

import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.googlepay.data.dto.CreateGooglePayTokenRequest
import com.paydock.feature.googlepay.data.dto.GooglePayTokenResponse
import com.paydock.feature.googlepay.data.mapper.asEntity
import com.paydock.feature.googlepay.domain.repository.GooglePayRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Implementation of the [GooglePayRepository] interface for handling Google Pay token operations.
 *
 * This class provides methods to create payment tokens from Google Pay payment data by performing
 * HTTP requests to the Paydock API.
 *
 * @param dispatcher The CoroutineDispatcher used for executing network calls.
 * @param client The [HttpClient] instance used to make HTTP requests.
 */
internal class GooglePayRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : GooglePayRepository {

    /**
     * Creates a payment token using the provided access token and Google Pay token.
     *
     * This method performs a POST request to the Paydock API to generate a payment token based on
     * the provided [CreateGooglePayTokenRequest]. It runs in the specified [dispatcher] context.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateGooglePayTokenRequest] containing the Google Pay token.
     * @return A [TokenDetails] object containing the generated payment token and its associated type.
     */
    override suspend fun createPaymentToken(
        accessToken: String,
        request: CreateGooglePayTokenRequest
    ): TokenDetails = withContext(dispatcher) {
        val httpResponse: HttpResponse = client.post {
            headers { append("x-access-token", accessToken) }
            url { path("/v1/payment_sources/tokens") }
            setBody(request)
        }
        httpResponse.body<GooglePayTokenResponse>().asEntity()
    }
}
