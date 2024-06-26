package com.paydock.feature.card.presentation.state

import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.feature.card.presentation.utils.CardPinValidator
import com.paydock.feature.card.presentation.utils.GiftCardNumberValidator

/**
 * Represents the current state of a gift card view.
 *
 * @property cardNumber The entered card number.
 * @property pin The entered PIN for the gift card.
 * @property token The token associated with the gift card, if available.
 * @property storePin Flag indicating whether the PIN should be stored.
 * @property isLoading Flag indicating whether the view is in a loading state.
 * @property error Any exception related to gift card operations that occurred.
 */
internal data class GiftCardViewState(
    val cardNumber: String = "",
    val pin: String = "",
    val token: String? = null,
    val storePin: Boolean = true,
    val isLoading: Boolean = false,
    val error: GiftCardException? = null,
) {
    /**
     * Get the validity status of all input data.
     */
    val isDataValid: Boolean
        get() = GiftCardNumberValidator.isCardNumberValid(cardNumber) &&
            CardPinValidator.checkPin(pin)
}
