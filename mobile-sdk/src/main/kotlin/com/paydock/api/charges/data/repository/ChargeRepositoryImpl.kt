package com.paydock.api.charges.data.repository

import com.paydock.api.charges.data.dto.CaptureChargeResponse
import com.paydock.api.charges.data.dto.CaptureWalletChargeRequest
import com.paydock.api.charges.data.dto.ChargeDeclineResponse
import com.paydock.api.charges.data.dto.WalletCallbackRequest
import com.paydock.api.charges.data.dto.WalletCallbackResponse
import com.paydock.api.charges.data.mapper.asEntity
import com.paydock.api.charges.domain.model.WalletCallback
import com.paydock.api.charges.domain.repository.ChargeRepository
import com.paydock.feature.charge.domain.model.integration.ChargeResponse
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
 * Implementation of the [ChargeRepository] interface for handling wallet-related charge operations.
 *
 * This class performs the actual network operations to capture, decline, and retrieve wallet callback data
 * using an HTTP client. It communicates with the backend services to execute these operations asynchronously.
 *
 * @property dispatcher The [CoroutineDispatcher] used to handle the asynchronous execution of network requests.
 * @property client The [HttpClient] used to make network requests.
 */
internal class ChargeRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : ChargeRepository {

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

}