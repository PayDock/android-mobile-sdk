package com.paydock.api.tokens.data.repository

import com.paydock.api.tokens.data.dto.CreatePaymentTokenRequest
import com.paydock.api.tokens.data.dto.CreateSessionTokenAuthRequest
import com.paydock.api.tokens.data.dto.CreateSetupTokenRequest
import com.paydock.api.tokens.data.dto.PaymentTokenResponse
import com.paydock.api.tokens.data.dto.SessionAuthTokenResponse
import com.paydock.api.tokens.data.dto.SetupTokenResponse
import com.paydock.api.tokens.data.mapper.asEntity
import com.paydock.api.tokens.domain.model.PayPalPaymentTokenDetails
import com.paydock.api.tokens.domain.model.SessionAuthToken
import com.paydock.api.tokens.domain.model.TokenDetails
import com.paydock.api.tokens.domain.repository.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Implementation of the [TokenRepository] interface for handling payment token operations
 * related to payment gateways, specifically for the PayPal API.
 *
 * This class provides methods to create payment tokens, session authentication tokens,
 * and setup tokens by performing HTTP requests to the PayPal API.
 *
 * @property dispatcher The CoroutineDispatcher used for executing network calls.
 * @property client The [HttpClient] instance used to make HTTP requests.
 */
internal class TokenRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : TokenRepository {

    /**
     * Creates a payment token using the provided access token and request data.
     *
     * This method performs a POST request to the Paydock API to generate a payment token based on
     * the provided [CreatePaymentTokenRequest]. It runs in the specified [dispatcher] context.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreatePaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [TokenDetails] object containing the generated payment token and its associated type.
     */
    override suspend fun createPaymentToken(
        accessToken: String,
        request: CreatePaymentTokenRequest
    ): TokenDetails = withContext(dispatcher) {
        val httpResponse: HttpResponse = client.post {
            headers { append("x-access-token", accessToken) }
            url { path("/v1/payment_sources/tokens") }
            setBody(request)
        }
        httpResponse.body<PaymentTokenResponse.CardTokenResponse>().asEntity()
    }

    /**
     * Creates a payment token using the provided access token and setup token along with the request data.
     *
     * This method should be implemented to send a request to the Paydock API to generate
     * a payment token based on the provided setup token and request information.
     *
     * @param accessToken The access token required for authorization.
     * @param setupToken The setup token that was previously generated to initialize the PayPal payment process.
     * @param request The [CreatePaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [TokenDetails] object containing the generated payment token and its associated type.
     */
    override suspend fun createPaymentToken(
        accessToken: String,
        setupToken: String,
        request: CreatePaymentTokenRequest.PayPalVaultRequest,
    ): PayPalPaymentTokenDetails = withContext(dispatcher) {
        val httpResponse: HttpResponse = client.post {
            headers { append("x-access-token", accessToken) }
            url { path("/v1/payment_sources/setup-tokens/$setupToken/tokens") }
            setBody(request)
        }
        httpResponse.body<PaymentTokenResponse.PayPalVaultTokenResponse>().asEntity()
    }

    /**
     * Creates a flow for generating a payment token asynchronously.
     *
     * This function performs a POST request to the PayPal API and emits the resulting
     * [TokenDetails] as a flow. This can be useful for observing changes or handling
     * updates in the token creation process.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreatePaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [Flow] emitting [TokenDetails] as the payment token is created.
     */
    override fun createPaymentTokenFlow(
        accessToken: String,
        request: CreatePaymentTokenRequest
    ): Flow<TokenDetails> = flow {
        emit(
            client.post {
                headers { append("x-access-token", accessToken) }
                url { path("/v1/payment_sources/tokens") }
                setBody(request)
            }.body<PaymentTokenResponse.CardTokenResponse>().asEntity()
        )
    }.flowOn(dispatcher)

    /**
     * Creates a session authentication token using the provided access token and request data.
     *
     * This function performs a POST request to the PayPal API to create an OAuth token for payment sources.
     * The resulting session authentication token can be used for further payment processing.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateSessionTokenAuthRequest] containing the necessary parameters for token creation.
     * @return A [SessionAuthToken] object containing the access token and ID token for the session.
     */
    override suspend fun createSessionAuthToken(
        accessToken: String,
        request: CreateSessionTokenAuthRequest
    ): SessionAuthToken = withContext(dispatcher) {
        val httpResponse: HttpResponse = client.post {
            headers { append("x-access-token", accessToken) }
            url { path("/v1/payment_sources/oauth-tokens") }
            setBody(request)
        }
        httpResponse.body<SessionAuthTokenResponse>().asEntity()
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