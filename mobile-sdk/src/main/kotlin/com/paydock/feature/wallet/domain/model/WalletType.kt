package com.paydock.feature.wallet.domain.model

/**
 * Enumeration representing different types of wallets.
 *
 * @property type The string representation of the wallet type.
 */
enum class WalletType(val type: String) {
    /**
     * Google Pay wallet type.
     */
    GOOGLE("google"),

    /**
     * PayPal wallet type.
     */
    PAY_PAL("paypal"),

    /**
     * Afterpay wallet type.
     */
    AFTER_PAY("afterpay"),

    /**
     * FlyPay wallet type.
     */
    FLY_PAY("flypay")
}
