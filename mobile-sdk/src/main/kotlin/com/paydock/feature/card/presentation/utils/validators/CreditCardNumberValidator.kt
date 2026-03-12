package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.CardScheme
import com.paydock.feature.card.presentation.utils.errors.CardNumberError

/**
 * Utility object for validating credit card numbers.
 *
 * Provides methods to validate the format, length, and correctness of credit card numbers,
 * including Luhn algorithm validation.
 *
 * **BIN data:** The [cardScheme] passed in is derived from BIN data that is always loaded
 * cache-first, then fallback to the bundled asset (card-schemes.json).
 */
internal object CreditCardNumberValidator {

    /**
     * Checks if a given credit card number is valid (e.g. for submit button).
     * Uses full validation with min length enforced (as on defocus).
     *
     * @param cardNumber The credit card number to validate.
     * @param cardScheme The detected card scheme, if available.
     * @param schemeConfig The configuration defining the supported card schemes and validation settings.
     * @return `true` if the card number is valid, `false` otherwise.
     */
    fun isCardNumberValid(
        cardNumber: String,
        cardScheme: CardScheme?,
        schemeConfig: SupportedSchemeConfig
    ) = validateCardNumberInput(
        cardNumber = cardNumber,
        cardScheme = cardScheme,
        schemeConfig = schemeConfig,
        hasUserInteracted = true,
        isCardNumberFocused = false
    ) == CardNumberError.None

    /**
     * Validates the credit card number input and determines the type of validation error.
     *
     * Validation behaviour:
     * - **Empty:** Blank input and user has interacted → [CardNumberError.Empty].
     * - **Luhn:** Only run when digit count is within [min, max] for the scheme (or 12–19 if no scheme).
     *   Outside that range no Luhn error is shown (avoids false positives while typing).
     * - **Length:** Too few digits → [CardNumberError.InvalidLength] (inline "Invalid card number") only when focus leaves
     *   the card number field (e.g. user taps expiry or CVV). Min/max use [CardScheme.lengths] when a scheme is detected,
     *   otherwise 12–19. Too many digits → [CardNumberError.InvalidLength]. No length/scheme errors while typing below [minLength].
     * - **Scheme:** When scheme validation is enabled and supported schemes are configured:
     *   - No scheme detected after 8 digits → [CardNumberError.UnsupportedCardScheme] ("Card type not accepted").
     *   - Detected scheme not in supported list → [CardNumberError.UnsupportedCardScheme] ("Card type not accepted").
     *
     * @param cardNumber The credit card number to validate (digits only in practice).
     * @param cardScheme The detected card scheme, if available. Supplies [CardScheme.lengths] for min/max; else 12–19.
     * @param schemeConfig The configuration defining the supported card schemes and validation settings.
     * @param hasUserInteracted Flag indicating if the user has interacted with the input field.
     * @param isCardNumberFocused True while the card number field is focused. Min-length error is only reported when false (on defocus).
     * @return A [CardNumberError] representing the validation result.
     */
    fun validateCardNumberInput(
        cardNumber: String,
        cardScheme: CardScheme?,
        schemeConfig: SupportedSchemeConfig,
        hasUserInteracted: Boolean,
        isCardNumberFocused: Boolean = false,
    ): CardNumberError {
        val digitLength = cardNumber.replace(Regex("\\D"), "").length
        val (minLength, maxLength) = getCardSchemeLengthRange(cardScheme?.lengths ?: emptyList())

        // 1. Empty
        if (cardNumber.isBlank() && hasUserInteracted) return CardNumberError.Empty

        // 2. Early unsupported scheme check: BIN detection completes at 8 digits, so show "Card type not accepted"
        //    as soon as we can determine the scheme is not supported (only when scheme validation is enabled).
        //    If no scheme is detected after 8 digits and validation is enabled, the card is not accepted.
        if (digitLength >= 8 && schemeConfig.enableValidation) {
            val supportedCardSchemes = getSupportedCardSchemes(schemeConfig)
            if (!supportedCardSchemes.isNullOrEmpty()) {
                // No scheme detected after 8 digits = unrecognized card type
                if (cardScheme == null) {
                    return CardNumberError.UnsupportedCardScheme
                }
                // Scheme detected but not in the supported list
                if (!supportedCardSchemes.contains(cardScheme.type)) {
                    return CardNumberError.UnsupportedCardScheme
                }
            }
        }

        // 3. Below min length: activate min-digit check only when focus leaves (e.g. user taps expiry/CVV).
        //    Uses scheme min/max if detected, else 12–19; shows inline "Invalid card number".
        if (digitLength < minLength) {
            if (!isCardNumberFocused && hasUserInteracted) return CardNumberError.InvalidLength
            return CardNumberError.None
        }

        // 4. Too many digits
        if (digitLength > maxLength) return CardNumberError.InvalidLength

        // 5. Within [min, max]: run Luhn
        if (!LuhnValidator.isLuhnValid(cardNumber)) return CardNumberError.InvalidLuhn

        return CardNumberError.None
    }

    /**
     * Returns the valid length range (min and max) for a given list of valid card lengths.
     *
     * If the provided list is empty, default length constraints are used.
     *
     * @param lengths The list of valid lengths for the card scheme.
     * @return A Pair representing the minimum and maximum valid lengths.
     */
    private fun getCardSchemeLengthRange(lengths: List<Int>): Pair<Int, Int> {
        return if (lengths.isNotEmpty()) {
            lengths.min() to lengths.max()
        } else {
            MobileSDKConstants.CardDetailsConfig.MIN_CREDIT_CARD_LENGTH to MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH
        }
    }

    /**
     * Retrieves the set of supported card schemes based on the provided configuration.
     *
     * If validation is enabled, returns the set of supported card schemes.
     * Otherwise, returns `null`, indicating that all card schemes are accepted without validation.
     *
     * @param schemeConfig The configuration specifying whether validation is enabled and the supported card schemes.
     * @return A set of supported [CardType]s if validation is enabled, or `null` if validation is disabled.
     */
    private fun getSupportedCardSchemes(schemeConfig: SupportedSchemeConfig): Set<CardType>? {
        return if (schemeConfig.enableValidation) schemeConfig.supportedSchemes else null
    }
}