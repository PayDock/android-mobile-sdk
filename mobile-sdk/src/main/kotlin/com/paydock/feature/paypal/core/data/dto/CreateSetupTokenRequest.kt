package com.paydock.feature.paypal.core.data.dto

import com.paydock.core.MobileSDKConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the request body for creating a setup token.
 *
 * This data class is used to serialize the request parameters needed for creating a setup token
 * via the PayPal API.
 *
 * @param gatewayId The gateway identifier for the payment provider.
 * @param returnUrl The deeplink scheme url to redirect the user to after completing the setup process.
 * @param cancelUrl The deeplink scheme url to redirect the user to after cancelling the setup process.
 */
@Serializable
internal data class CreateSetupTokenRequest(
    @SerialName("gateway_id") val gatewayId: String,
    @SerialName("return_url") val returnUrl: String = MobileSDKConstants.PayPalVaultConfig.RETURN_URL,
    @SerialName("cancel_url") val cancelUrl: String = MobileSDKConstants.PayPalVaultConfig.CANCEL_URL
)
