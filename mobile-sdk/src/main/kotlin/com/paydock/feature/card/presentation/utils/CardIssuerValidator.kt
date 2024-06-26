package com.paydock.feature.card.presentation.utils

import com.paydock.feature.card.presentation.model.CardIssuerType

/**
 * A utility object for detecting card issuer based on card numbers
 */
internal object CardIssuerValidator {
    /**
     * Constants for the regex patterns and their corresponding card issuers
     *
     * @see CardIssuerType.VISA - Visa cards begin with a 4 and have 13-16-19-digit
     * @see CardIssuerType.MASTERCARD - Mastercard cards begin with a 5 and has 16 digits (51, 52, 53, 54, 55, 222100-272099)
     * @see CardIssuerType.AMERICAN_EXPRESS - American Express cards begin with a 3, followed by a 4 or a 7 has 15 digits
     * @see CardIssuerType.DINERS_CLUB (Diners Club - Carte Blanche) - is a 14-digit number beginning with 300–305,
     * @see CardIssuerType.DINERS_CLUB (Diners Club - International) - is a 14-digit number beginning with 36, 38, or
     * @see CardIssuerType.DINERS_CLUB (Diners Club - USA & Canada) - is a 16-digit number beginning with 54
     * @see CardIssuerType.JCB - JCB CCN is a 16-19-digit number beginning with 3528 or 3589.
     * @see CardIssuerType.MAESTRO - Every Maestro card number is a 16-digit number has a specifically prefix 50, 56-69
     * @see CardIssuerType.DISCOVER - Credit Card Number (Discover) is a 16-19-digit number beginning with 6011, 644–649 or 65.
     * @see CardIssuerType.UNION_PAY - The China UnionPay credit card numbers begin with 62 or 60 and is a 16-19 digit long number.
     * @see CardIssuerType.INTER_PAY - INTER_PAY CCN is a 16-digit number beginning with 636.
     * @see CardIssuerType.INSTA_PAYMENT - Credit Card Number (InstaPayment) is a 16-digit number beginning with 637, 638, 639.
     * @see CardIssuerType.UATP - The UATP credit card numbers start with 1 and is 15 digits long
     */
    private val cardIssuerRegexMap = mapOf(
        Regex("^3[47][0-9]{13}$") to CardIssuerType.AMERICAN_EXPRESS,
        Regex("^3(?:0[0-59]|[689])[0-9]*\$") to CardIssuerType.DINERS_CLUB,
        Regex("^4[0-9]*$") to CardIssuerType.VISA,
        Regex("^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[01]|2720)[0-9]*$") to CardIssuerType.MASTERCARD,
        Regex("^6(?:011|5[0-9]{2})[- ]?[0-9]{4}[- ]?[0-9]{4}[- ]?[0-9]{4,7}$") to CardIssuerType.DISCOVER,
        Regex("^62[0-9]*$") to CardIssuerType.UNION_PAY,
        Regex("^636[0-9]*$") to CardIssuerType.INTER_PAY,
        Regex("^63[7-9][0-9]*$") to CardIssuerType.INSTA_PAYMENT,
        Regex("^(?:2131|1800|35)[0-9]*$") to CardIssuerType.JCB,
        Regex("^(5[06789]|6)[0-9]{11,18}$") to CardIssuerType.MAESTRO,
        Regex("^1[0-9]*$") to CardIssuerType.UATP,
        // The generic pattern for all other card issuers
        Regex(".*") to CardIssuerType.OTHER
    )

    /**
     * Detects the card issuer based on the provided credit card number using regex patterns.
     *
     * @param number The credit card number to detect the issuer for.
     * @return The [CardIssuerType] enum representing the detected card issuer, or [CardIssuerType.OTHER] if no issuer is matched.
     */
    fun detectCardIssuer(number: String): CardIssuerType {
        for ((regex, issuer) in cardIssuerRegexMap) {
            if (regex.find(number) != null) {
                return issuer
            }
        }
        return CardIssuerType.OTHER
    }
}