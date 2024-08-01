package com.paydock.feature.afterpay.presentation.state

import com.paydock.core.domain.error.exceptions.AfterpayException
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.domain.model.WalletCallback

/**
 * Represents the state of the Afterpay payment view.
 *
 * @property isLoading Indicates if the view is in a loading state.
 * @property error Represents any error exception that occurred during the Afterpay transaction.
 * @property validLocale Flag to determine if Locale is valid for Afterpay.
 * @property token The token associated with the wallet.
 * @property chargeData The [ChargeResponse] instance holding the wallet data.
 * @property callbackData The [WalletCallback] for the wallet callback details, if available.
 */
internal data class AfterpayViewState(
    val isLoading: Boolean = false,
    val error: AfterpayException? = null,
    val validLocale: Boolean = false,
    val token: String? = null,
    val chargeData: ChargeResponse? = null,
    val callbackData: WalletCallback? = null,
)