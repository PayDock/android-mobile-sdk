package com.paydock.feature.paypal.checkout.domain.model.integration

/**
 * Configuration for the PayPal widget integration.
 *
 * This data class holds various configuration options that can be used to customize the behavior
 * of the PayPal widget when presented to the user. It is crucial for initiating and managing
 * PayPal transactions within the application.
 *
 * @property accessToken The OAuth access token required for authenticating API requests to the PayPal services.
 * This token ensures secure communication and authorizes operations on behalf of the user or merchant.
 * @property gatewayId The unique identifier for the payment gateway used to route transactions.
 * This ID directs payments through the correct processing channels.
 * @property requestShipping A boolean indicating whether shipping information should be requested
 * from the user during the PayPal checkout flow. Defaults to `true`. Setting this to `false`
 * can streamline the checkout process for digital goods or services where shipping is not applicable.
 * @property fundingSource An enum to specify the type of preset funding for a PayPal order.
 */
data class PayPalWidgetConfig(
    val accessToken: String,
    val gatewayId: String,
    val requestShipping: Boolean = true,
    val fundingSource: PayPalFundingSource = PayPalFundingSource.PAYPAL
) {

    /**
     * Enum class representing different types of PayPal funding sources that can be chosen.
     */
    enum class PayPalFundingSource {

        /**
         * PAYPAL_CREDIT will launch the web checkout flow and display PayPal Credit funding to
         * eligible customers Eligible costumers receive a revolving line of credit that
         * they can use to pay over time.
         */
        PAYPAL_CREDIT,

        /**
         * PAY_LATER will launch the web checkout flow and display Pay Later offers to
         * eligible customers, which include short-term, interest-free payments and other
         * special financing options.
         */
        PAY_LATER,

        /**
         * PAYPAL will launch the web checkout for a one-time PayPal Checkout flow.
         */
        PAYPAL
    }
}