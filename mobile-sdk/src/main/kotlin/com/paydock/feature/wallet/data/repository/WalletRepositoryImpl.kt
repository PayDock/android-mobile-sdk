package com.paydock.feature.wallet.data.repository

import com.paydock.feature.wallet.data.dto.CaptureChargeResponse
import com.paydock.feature.wallet.data.dto.CaptureWalletChargeRequest
import com.paydock.feature.wallet.data.dto.ChargeDeclineResponse
import com.paydock.feature.wallet.data.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.dto.WalletConfigResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.repository.WalletRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Implementation of the [WalletRepository] interface for handling wallet-related charge operations.
 *
 * This class performs the actual network operations to capture, decline, and retrieve wallet callback data
 * using an HTTP client. It communicates with the backend services to execute these operations asynchronously.
 *
 * @property dispatcher The [CoroutineDispatcher] used to handle the asynchronous execution of network requests.
 * @property client The [HttpClient] used to make network requests.
 */
internal class WalletRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : WalletRepository {

    /**
     * Captures a wallet transaction using the provided token and [CaptureWalletChargeRequest].
     *
     * Sends a POST request to capture a charge on a wallet transaction, returning the result
     * as a [ChargeResponse].
     *
     * @param token The access token used for authorization.
     * @param request The request data containing the details of the wallet charge to capture.
     * @return The [ChargeResponse] containing the result of the capture operation.
     */
    override suspend fun captureWalletTransaction(
        token: String,
        request: CaptureWalletChargeRequest
    ): ChargeResponse =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.post {
                headers { append("x-access-token", token) }
                url { path("/v1/charges/wallet/capture") }
                setBody(request)
            }
            httpResponse.body<CaptureChargeResponse>().asEntity()
        }

    /**
     * Declines a wallet charge using the provided token and charge ID.
     *
     * Sends a POST request to decline a previously initiated charge, returning the result
     * as a [ChargeResponse].
     *
     * @param token The access token used for authorization.
     * @param chargeId The ID of the charge to be declined.
     * @return The [ChargeResponse] containing the result of the decline operation.
     */
    override suspend fun declineWalletCharge(token: String, chargeId: String): ChargeResponse =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.post {
                headers { append("x-access-token", token) }
                url { path("/v1/charges/wallet/$chargeId/decline") }
            }
            httpResponse.body<ChargeDeclineResponse>().asEntity()
        }

    /**
     * Retrieves wallet callback data using the provided token and [WalletCallbackRequest].
     *
     * Sends a POST request to retrieve wallet callback data, including the callback URL
     * and any related transaction status, returning the result as a [WalletCallback].
     *
     * @param token The access token used for authorization.
     * @param request The request data containing the details for fetching the callback.
     * @return The [WalletCallback] containing the callback data.
     */
    override suspend fun getWalletCallback(
        token: String,
        request: WalletCallbackRequest
    ): WalletCallback =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.post {
                headers { append("x-access-token", token) }
                url {
                    path("/v1/charges/wallet/callback")
                    parameters.append("mobile", "true")
                }
                setBody(request)
            }
            httpResponse.body<WalletCallbackResponse>().asEntity()
        }

    /**
     * Retrieves the Payment Source client ID for a specific wallet payment gateway.
     *
     * This function sends a GET request to fetch the client ID associated with the provided gateway
     * identifier. It uses the provided OAuth token for authentication. The client ID is required to
     * initiate gateway transactions.
     *
     * @param accessToken The OAuth token used for authenticating the request.
     * @param gatewayId The identifier for the payment gateway.
     * @return The client ID if available, or throw an exception if the client ID could not be retrieved.
     */
    override suspend fun getWalletGatewayClientId(accessToken: String, gatewayId: String): String =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.get {
                headers { append("x-access-token", accessToken) }
                url { path("/v1/gateways/$gatewayId/wallet-config") }
            }
            httpResponse.body<WalletConfigResponse>().resource.data?.credentials?.clientAuth
                ?: error(IllegalStateException("Client ID not found in response."))
        }

}