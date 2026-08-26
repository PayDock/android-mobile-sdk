package com.paydock.feature.card.presentation.state

import com.paydock.feature.card.presentation.utils.validators.CardPinValidator
import com.paydock.feature.card.presentation.utils.validators.GiftCardNumberValidator

/**
 * Represents the input state for a gift card, including the card number, PIN, and whether the PIN should be stored.
 *
 * @property cardNumber The gift card number entered by the user.
 * @property pin The PIN associated with the gift card.
 * @property storePin Indicates whether the PIN should be stored (default is `true`).
 * @property cardNumberErrorOverwrite A flag to force showing validation errors for the card number field. Defaults to `false`.
 * @property pinErrorOverwrite A flag to force showing validation errors for the PIN field. Defaults to `false`.
 */
internal data class GiftCardInputState(
    val cardNumber: String = "",
    val pin: String = "",
    val storePin: Boolean = true,
    val cardNumberErrorOverwrite: Boolean = false,
    val pinErrorOverwrite: Boolean = false,
) {
    /**
     * Enum representing the input fields in the gift card form.
     */
    enum class GiftCardField {
        CARD_NUMBER,
        PIN
    }

    /**
     * Returns a list of invalid fields based on the current state, in visual/tab order.
     *
     * Memoized with [lazy]: `isDataValid`/`errorCount` and the widget both read this on every
     * recomposition of an (immutable) instance, and re-running both validators each time is wasted
     * work once the first read has already computed the answer for this exact input.
     */
    val invalidFields: List<GiftCardField> by lazy {
        mutableListOf<GiftCardField>().apply {
            if (!GiftCardNumberValidator.isCardNumberValid(cardNumber)) {
                add(GiftCardField.CARD_NUMBER)
            }
            if (!CardPinValidator.isCardPinValid(pin)) {
                add(GiftCardField.PIN)
            }
        }
    }

    /**
     * Returns the total number of validation errors in the form.
     */
    val errorCount: Int
        get() = invalidFields.size

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
        get() = errorCount == 0

    // Overridden so cardholder-data (card number, PIN) never lands in a log or crash report via the
    // default data-class toString() — e.g. if this state is passed to Log.d/logcat or serialized into
    // an exception message.
    override fun toString(): String =
        "GiftCardInputState(cardNumber=[REDACTED], pin=[REDACTED], storePin=$storePin, " +
            "cardNumberErrorOverwrite=$cardNumberErrorOverwrite, pinErrorOverwrite=$pinErrorOverwrite)"
}