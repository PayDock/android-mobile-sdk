package com.paydock.feature.googlepay.presentation.state

import com.google.android.gms.wallet.PaymentData
import com.paydock.core.domain.error.ErrorModel
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.feature.charge.domain.model.ChargeResponse

/**
 * Represents the state of the Google Pay feature in the application.
 *
 * @param googlePayAvailable Indicates whether Google Pay is available and can be used.
 * @param paymentData The payment data received from a successful Google Pay transaction.
 * @param isLoading Indicates whether a loading state is active (determines whether the Google Pay button is clickable).
 * @param error An [ErrorModel] that holds information about any errors encountered.
 * @property token The token associated with the wallet.
 */
internal data class GooglePayViewState(
    val googlePayAvailable: Boolean = false,
    val paymentData: PaymentData? = null,
    val chargeData: ChargeResponse? = null,
    val isLoading: Boolean = false,
    val error: GooglePayException? = null,
    val token: String? = null,
)