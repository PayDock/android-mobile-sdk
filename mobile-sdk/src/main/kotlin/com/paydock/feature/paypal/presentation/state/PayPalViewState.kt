package com.paydock.feature.paypal.presentation.state

import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.domain.model.WalletCallback

/**
 * Represents the state of the PayPal payment view.
 *
 * @property isLoading Indicates if the view is in a loading state.
 * @property error Represents any error exception that occurred during the PayPal transaction.
 * @property token The token associated with the wallet.
 * @property chargeData The [ChargeResponse] instance holding the wallet data.
 * @property callbackData The [WalletCallback] for the wallet callback details, if available.
 * @property paymentData Contains the PayPal payment data, if available.
 */
internal data class PayPalViewState(
    val isLoading: Boolean = false,
    val error: PayPalException? = null,
    val token: String? = null,
    val chargeData: ChargeResponse? = null,
    val callbackData: WalletCallback? = null,
    val paymentData: PayPalData? = null
)

/**
 * Represents the data related to a PayPal payment.
 *
 * @property payPalToken The PayPal token related to the payment.
 * @property payerId The PayerID related to the PayPal payment.
 */
internal data class PayPalData(
    var payPalToken: String,
    var payerId: String
)
