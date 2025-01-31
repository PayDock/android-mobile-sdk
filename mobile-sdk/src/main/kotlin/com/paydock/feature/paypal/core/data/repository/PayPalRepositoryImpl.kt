package com.paydock.feature.paypal.core.data.repository

import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.paypal.core.data.dto.CreateSetupTokenRequest
import com.paydock.feature.paypal.core.data.dto.PayPalVaultTokenRequest
import com.paydock.feature.paypal.core.data.dto.PayPalVaultTokenResponse
import com.paydock.feature.paypal.core.data.dto.SetupTokenResponse
import com.paydock.feature.paypal.core.data.mapper.asEntity
import com.paydock.feature.paypal.core.domain.model.ui.PayPalPaymentTokenDetails
import com.paydock.feature.paypal.core.domain.repository.PayPalRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class PayPalRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : PayPalRepository {

    /**
     * Creates a payment token using the provided access token and setup token along with the request data.
     *
     * This method should be implemented to send a request to the Paydock API to generate
     * a payment token based on the provided setup token and request information.
     *
     * @param accessToken The access token required for authorization.
     * @param setupToken The setup token that was previously generated to initialize the PayPal payment process.
     * @param request The [CreateCardPaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [TokenDetails] object containing the generated payment token and its associated type.
     */
    override suspend fun createPaymentToken(
        accessToken: String,
        setupToken: String,
        request: PayPalVaultTokenRequest,
    ): PayPalPaymentTokenDetails = withContext(dispatcher) {
        val httpResponse: HttpResponse = client.post {
            headers { append("x-access-token", accessToken) }
            url { path("/v1/payment_sources/setup-tokens/$setupToken/tokens") }
            setBody(request)
        }
        httpResponse.body<PayPalVaultTokenResponse>().asEntity()
    }

    /**
     * Creates a setup token for PayPal payment sources.
     *
     * This function makes an HTTP POST request to the PayPal API to create a setup token using the provided
     * access token and request body. The setup token is used for setting up future payments.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateSetupTokenRequest] containing the necessary data for creating a setup token.
     * @return A nullable [String] representing the setup token if the operation is successful,
     *         or null if the token could not be created.
     */
    override suspend fun createSetupToken(
        accessToken: String,
        request: CreateSetupTokenRequest
    ): String = withContext(dispatcher) {
        val httpResponse: HttpResponse = client.post {
            headers { append("x-access-token", accessToken) }
            url { path("/v1/payment_sources/setup-tokens") }
            setBody(request)
        }
        httpResponse.body<SetupTokenResponse>().resource.data?.setupToken
            ?: error("Setup token not found in response.")
    }
}