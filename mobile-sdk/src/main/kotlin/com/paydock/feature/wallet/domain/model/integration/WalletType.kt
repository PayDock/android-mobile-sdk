package com.paydock.feature.wallet.domain.model.integration

/**
 * Enumeration representing different types of wallets.
 *
 * @property type The string representation of the wallet type.
 */
enum class WalletType(val type: String) {

    /**
     * PayPal wallet type.
     */
    PAY_PAL("paypal"),

    /**
     * Afterpay wallet type.
     */
    AFTER_PAY("afterpay"),

    /**
     * Coles Pay wallet type.
     */
    COLES_PAY("flypay_v2")
}
