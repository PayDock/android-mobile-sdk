package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.domain.model.integration.enums.CardScheme

/**
 * A utility object for detecting card scheme based on card numbers
 */
internal object CardSchemeValidator {
    /**
     * Constants for the regex patterns and their corresponding card schemes
     *
     * @see CardScheme.VISA - Visa cards begin with a 4 and have 13-16-19-digit
     * @see CardScheme.MASTERCARD - Mastercard cards begin with a 5 and has 16 digits (51, 52, 53, 54, 55, 222100-272099)
     * @see CardScheme.AMEX - American Express cards begin with a 3, followed by a 4 or a 7 has 15 digits
     * @see CardScheme.DINERS (Diners Club - Carte Blanche) - is a 14-digit number beginning with 300–305,
     * @see CardScheme.DINERS (Diners Club - International) - is a 14-digit number beginning with 36, 38, or
     * @see CardScheme.DINERS (Diners Club - USA & Canada) - is a 16-digit number beginning with 54
     * @see CardScheme.JAPCB - JCB CCN is a 16-19-digit number beginning with 3528 or 3589.
     * @see CardScheme.DISCOVER - Credit Card Number (Discover) is a 16-19-digit number beginning with 6011, 644–649 or 65.
     * @see CardScheme.SOLO - The SOLO card numbers which can have 16, 18, or 19 digits. Starts with either "6334" or "6767"
     * @see CardScheme.AUSBC - The AUSBC numbers which Matches the Issuer Identification Numbers (IINs)
     * commonly associated with Australian Bank Cards
     */
    @Suppress("MaxLineLength")
    private val cardSchemeRegexMap = mapOf(
        Regex("^3[47][0-9]{13}\$") to CardScheme.AMEX,
        Regex("^3(?:0[0-5]|[68][0-9])[0-9]{11}\$") to CardScheme.DINERS,
        Regex("^4[0-9]{12}(?:[0-9]{3,6})?\$") to CardScheme.VISA,
        Regex("^(5[1-5][0-9]{14}|2(22[1-9][0-9]{12}|2[3-9][0-9]{13}|[3-6][0-9]{14}|7[0-1][0-9]{13}|720[0-9]{12}))\$") to CardScheme.MASTERCARD,
        Regex("^6(?:011|5[0-9]{2})[0-9]{12,15}\$") to CardScheme.DISCOVER,
        Regex("^(?:2131|1800|35\\d{3})\\d{11}$") to CardScheme.JAPCB,
        Regex("^(6334|6767)[0-9]{12}|(6334|6767)[0-9]{14}|(6334|6767)[0-9]{15}\$") to CardScheme.SOLO,
        Regex("^(5893|6304|677189|67719[0-9])[0-9]{8,15}\$") to CardScheme.AUSBC,
        // The generic pattern for all other card schemes
        Regex(".*") to null
    )

    /**
     * Detects the card schemes based on the provided credit card number using regex patterns.
     *
     * @param number The credit card number to detect the schemes for.
     * @return The [CardScheme] enum representing the detected card schemes, or [CardScheme.UNKNOWN] if no scheme is matched.
     */
    fun detectCardScheme(number: String): CardScheme? {
        for ((regex, scheme) in cardSchemeRegexMap) {
            if (regex.find(number) != null) {
                return scheme
            }
        }
        return null
    }
}