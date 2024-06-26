package com.paydock.feature.wallet.domain.repository

import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.api.dto.WalletCaptureRequest
import com.paydock.feature.wallet.data.api.dto.WalletCaptureResponse
import com.paydock.feature.wallet.domain.model.WalletCallback

/**
 * A repository interface responsible for capturing wallet transactions.
 */
internal interface WalletRepository {

    /**
     * Capture a wallet transaction with the provided token and request details.
     *
     * @param token The authentication token required for the transaction.
     * @param request The request object containing transaction details.
     * @return A [WalletCaptureResponse] representing the result of the transaction capture.
     */
    suspend fun captureWalletTransaction(
        token: String,
        request: WalletCaptureRequest
    ): ChargeResponse

    /**
     * Decline a wallet transaction with the provided chargeId.
     *
     * @param token The authentication token required for the transaction.
     * @param chargeId The chargeId required for the transaction.
     * @return A [WalletCaptureResponse] representing the result of the transaction decline.
     */
    suspend fun declineWalletTransaction(token: String, chargeId: String): ChargeResponse

    /**
     * Retrieve wallet callback information from the server.
     *
     * @param token The authentication token for accessing the wallet callback information.
     * @param request The request object specifying the details of the wallet callback request.
     *
     * @return A callback details representing the wallet callback information retrieved from the server.
     */
    suspend fun getWalletCallback(token: String, request: WalletCallbackRequest): WalletCallback

}
