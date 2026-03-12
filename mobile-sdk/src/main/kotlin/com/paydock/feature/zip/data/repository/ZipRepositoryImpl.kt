package com.paydock.feature.zip.data.repository

import com.paydock.feature.zip.data.dto.ExternalCheckoutResponse
import com.paydock.feature.zip.data.dto.PaymentSourceTokenRequest
import com.paydock.feature.zip.data.dto.PaymentSourceTokenResponse
import com.paydock.feature.zip.data.mapper.toExternalCheckoutRequest
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.domain.repository.ZipRepository
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
 * Implementation of [ZipRepository] for handling Zip payment operations.
 *
 * This class performs HTTP requests to the Paydock API to manage Zip checkout sessions
 * and payment source token creation.
 *
 * @param dispatcher The CoroutineDispatcher used for executing network calls.
 * @param client The [HttpClient] instance used to make HTTP requests.
 */
internal class ZipRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : ZipRepository {

    /**
     * Initializes an external checkout session with Zip.
     *
     * Performs a POST request to create a checkout session and returns
     * the checkout URL and token needed to complete the payment flow.
     *
     * @param accessToken The access token for API authentication.
     * @param config The Zip widget configuration containing payment details.
     * @return A pair containing the checkout URL (link) and checkout token.
     */
    override suspend fun initializeExternalCheckout(
        accessToken: String,
        config: ZipWidgetConfig
    ): Pair<String, String> = withContext(dispatcher) {
        val request = config.toExternalCheckoutRequest()
        val httpResponse: HttpResponse = client.post {
            headers { append("x-access-token", accessToken) }
            url { path("/v1/payment_sources/external_checkout") }
            setBody(request)
        }
        val response = httpResponse.body<ExternalCheckoutResponse>()
        Pair(response.resource.data.link, response.resource.data.token)
    }

    /**
     * Creates a payment source token from the checkout token.
     *
     * Performs a POST request to convert the checkout token into a payment source token
     * after the user completes the Zip checkout flow.
     *
     * @param accessToken The access token for API authentication.
     * @param checkoutToken The checkout token received from the Zip checkout flow.
     * @param gatewayId The gateway ID for processing.
     * @return The payment source token string.
     */
    override suspend fun createPaymentSourceToken(
        accessToken: String,
        checkoutToken: String,
        gatewayId: String
    ): String = withContext(dispatcher) {
        val request = PaymentSourceTokenRequest(
            checkoutToken = checkoutToken,
            gatewayId = gatewayId
        )
        val httpResponse: HttpResponse = client.post {
            headers { append("x-access-token", accessToken) }
            url { path("/v1/payment_sources/tokens") }
            setBody(request)
        }
        val response = httpResponse.body<PaymentSourceTokenResponse>()
        response.resource.data
    }
}
