package com.paydock.feature.wallet.data.repository

import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.api.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.api.dto.WalletCaptureRequest
import com.paydock.feature.wallet.data.api.dto.WalletCaptureResponse
import com.paydock.feature.wallet.data.api.dto.WalletDeclineResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.model.WalletCallback
import com.paydock.feature.wallet.domain.repository.WalletRepository
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
 * Implementation of the [WalletRepository] interface responsible for capturing wallet transactions.
 *
 * @param dispatcher The [CoroutineDispatcher] used to control the execution context.
 * @param client The [HttpClient] for making HTTP requests.
 */
internal class WalletRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : WalletRepository {

    /**
     * Capture a wallet transaction with the provided token and request details.
     *
     * @param token The authentication token required for the transaction.
     * @param request The request object containing transaction details.
     * @return A [WalletCaptureResponse] representing the result of the transaction capture.
     */
    override suspend fun captureWalletTransaction(
        token: String,
        request: WalletCaptureRequest
    ): ChargeResponse =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.post {
                headers {
                    append("x-access-token", token)
                    append("Content-Type", "application/json;charset=utf-8")
                }
                url { path("/v1/charges/wallet/capture") }
                setBody(request)
            }
            httpResponse.body<WalletCaptureResponse>().asEntity()
        }

    /**
     * Decline a wallet transaction with the provided chargeId.
     *
     * @param token The authentication token required for the transaction.
     * @param chargeId The chargeId required for the transaction.
     * @return A [WalletCaptureResponse] representing the result of the transaction decline.
     */
    override suspend fun declineWalletTransaction(token: String, chargeId: String): ChargeResponse =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.post {
                headers {
                    append("x-access-token", token)
                    append("Content-Type", "application/json;charset=utf-8")
                }
                url { path("/v1/charges/wallet/$chargeId/decline") }
            }
            httpResponse.body<WalletDeclineResponse>().asEntity()
        }

    /**
     * Retrieve wallet callback information from the server.
     *
     * @param token The authentication token for accessing the wallet callback information.
     * @param request The request object specifying the details of the wallet callback request.
     *
     * @return A callback url representing the wallet callback information retrieved from the server.
     */
    override suspend fun getWalletCallback(
        token: String,
        request: WalletCallbackRequest
    ): WalletCallback =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.post {
                headers {
                    append("x-access-token", token)
                    append("Content-Type", "application/json;charset=utf-8")
                }
                url {
                    path("/v1/charges/wallet/callback")
                    parameters.append("mobile", "true")
                }
                setBody(request)
            }
            httpResponse.body<WalletCallbackResponse>().asEntity()
        }

}
