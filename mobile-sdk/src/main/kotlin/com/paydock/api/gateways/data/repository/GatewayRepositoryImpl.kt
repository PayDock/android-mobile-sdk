package com.paydock.api.gateways.data.repository

import com.paydock.api.gateways.data.dto.WalletConfigResponse
import com.paydock.api.gateways.domain.repository.GatewayRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Implementation of the `GatewayRepository` interface that handles interaction with payment gateways.
 *
 * The `GatewayRepositoryImpl` class uses an `HttpClient` to send network requests and retrieve data
 * from backend services related to payment gateways. It performs these operations in a background
 * thread defined by the provided `CoroutineDispatcher`.
 *
 * @property dispatcher The coroutine dispatcher used to offload network operations to a background thread.
 * @property client The HTTP client used to perform network requests.
 */
internal class GatewayRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : GatewayRepository {

    /**
     * Retrieves the Payment Source client ID for a specific payment gateway.
     *
     * This function sends a GET request to fetch the client ID associated with the provided gateway
     * identifier. It uses the provided OAuth token for authentication. The client ID is required to
     * initiate gateway transactions.
     *
     * @param accessToken The OAuth token used for authenticating the request.
     * @param gatewayId The identifier for the payment gateway.
     * @return The client ID if available, or throw an exception if the client ID could not be retrieved.
     */
    override suspend fun getGatewayClientId(accessToken: String, gatewayId: String): String =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.get {
                headers { append("x-access-token", accessToken) }
                url { path("/v1/gateways/$gatewayId/wallet-config") }
            }
            httpResponse.body<WalletConfigResponse>().resource.data?.credentials?.clientAuth
                ?: error(IllegalStateException("Client ID not found in response."))
        }
}