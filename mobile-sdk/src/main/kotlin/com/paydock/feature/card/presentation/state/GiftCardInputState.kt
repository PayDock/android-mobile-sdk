package com.paydock.feature.card.presentation.state

import com.paydock.feature.card.presentation.utils.validators.CardPinValidator
import com.paydock.feature.card.presentation.utils.validators.GiftCardNumberValidator

/**
 * Represents the input state for a gift card, including the card number, PIN, and whether the PIN should be stored.
 *
 * @property cardNumber The gift card number entered by the user.
 * @property pin The PIN associated with the gift card.
 * @property storePin Indicates whether the PIN should be stored (default is `true`).
 */
internal data class GiftCardInputState(
    val cardNumber: String = "",
    val pin: String = "",
    val storePin: Boolean = true,
) {
    /**
     * Checks if the current input state is valid.
     *
     * The data is considered valid if:
     * - The card number passes the validation defined by `GiftCardNumberValidator.isCardNumberValid`.
     * - The PIN passes the validation defined by `CardPinValidator.checkPin`.
     *
     * @return `true` if both the card number and PIN are valid; otherwise, `false`.
     */
    val isDataValid: Boolean
        get() = GiftCardNumberValidator.isCardNumberValid(cardNumber) &&
            CardPinValidator.isCardPinValid(pin)
}