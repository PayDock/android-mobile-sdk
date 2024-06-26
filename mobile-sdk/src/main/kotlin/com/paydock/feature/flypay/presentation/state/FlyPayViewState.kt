package com.paydock.feature.flypay.presentation.state

import com.paydock.core.domain.error.exceptions.FlyPayException
import com.paydock.feature.wallet.domain.model.WalletCallback

/**
 * Represents the state of the FlyPay view in the application.
 *
 * @property isLoading A boolean indicating whether the wallet data is currently being loaded.
 * @property error Represents any error exception that occurred during the FlyPay transaction.
 * @property token The token associated with the wallet.
 * @property callbackData The details for the wallet callback, if available.
 */
internal data class FlyPayViewState(
    val isLoading: Boolean = false,
    val error: FlyPayException? = null,
    val token: String? = null,
    val callbackData: WalletCallback? = null
)