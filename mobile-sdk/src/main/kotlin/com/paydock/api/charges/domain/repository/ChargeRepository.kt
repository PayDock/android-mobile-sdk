package com.paydock.api.charges.domain.repository

import com.paydock.api.charges.data.dto.CaptureWalletChargeRequest
import com.paydock.api.charges.data.dto.WalletCallbackRequest
import com.paydock.api.charges.domain.model.WalletCallback
import com.paydock.feature.charge.domain.model.integration.ChargeResponse

/**
 * Repository interface for handling charge-related operations.
 *
 * The [ChargeRepository] interface defines methods to capture, decline,
 * and retrieve wallet charge and transaction data. Implementations of this
 * interface handle communication with the data source (e.g., network or database)
 * to perform operations related to wallet transactions.
 */
internal interface ChargeRepository {

    /**
     * Captures a wallet transaction for a given token and request.
     *
     * This method triggers the capture of a charge against a wallet transaction
     * using the provided token and [CaptureWalletChargeRequest]. The result is returned
     * as a [ChargeResponse].
     *
     * @param token The access token used for authorization.
     * @param request The request data containing details for capturing the charge.
     * @return The [ChargeResponse] containing the result of the capture.
     */
    suspend fun captureWalletTransaction(
        token: String,
        request: CaptureWalletChargeRequest
    ): ChargeResponse

    /**
     * Declines a wallet charge for a given token and charge ID.
     *
     * This method is used to decline a previously initiated wallet charge
     * using the provided token and charge ID. The result is returned as a [ChargeResponse].
     *
     * @param token The access token used for authorization.
     * @param chargeId The ID of the charge to be declined.
     * @return The [ChargeResponse] containing the result of the decline operation.
     */
    suspend fun declineWalletCharge(token: String, chargeId: String): ChargeResponse

    /**
     * Retrieves wallet callback data for a given token and request.
     *
     * This method retrieves the callback data related to a wallet transaction,
     * including any necessary callback URLs and status information. The result
     * is returned as a [WalletCallback].
     *
     * @param token The access token used for authorization.
     * @param request The request data containing details for fetching the wallet callback.
     * @return The [WalletCallback] containing the retrieved callback data.
     */
    suspend fun getWalletCallback(token: String, request: WalletCallbackRequest): WalletCallback

}
