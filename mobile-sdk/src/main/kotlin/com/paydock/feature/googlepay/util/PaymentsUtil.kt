package com.paydock.feature.googlepay.util

import com.google.android.gms.wallet.PaymentData
import com.paydock.core.MobileSDKConstants
import com.paydock.feature.googlepay.domain.model.integration.GooglePayBillingAddressParameters
import com.paydock.feature.googlepay.domain.model.integration.GooglePayCardParameters
import com.paydock.feature.googlepay.domain.model.integration.GooglePayCardPaymentMethod
import com.paydock.feature.googlepay.domain.model.integration.GooglePayIsReadyToPayRequest
import com.paydock.feature.googlepay.domain.model.integration.GooglePayMerchantInfo
import com.paydock.feature.googlepay.domain.model.integration.GooglePayPaymentDataRequest
import com.paydock.feature.googlepay.domain.model.integration.GooglePayPaymentMethodTokenizationParameters
import com.paydock.feature.googlepay.domain.model.integration.GooglePayPaymentMethodTokenizationSpecification
import com.paydock.feature.googlepay.domain.model.integration.GooglePayPaymentResponse
import com.paydock.feature.googlepay.domain.model.integration.GooglePayShippingAddressParameters
import com.paydock.feature.googlepay.domain.model.integration.GooglePayTransactionInfo
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility object responsible for handling Google Pay logic and configuration.
 */
object PaymentsUtil {

    /**
     * Create a Google Pay payment request based on provided parameters.
     *
     * @param amount The payment amount.
     * @param amountLabel The label for the payment amount.
     * @param countryCode The country code.
     * @param currencyCode The currency code.
     * @param merchantName The merchant name.
     * @param merchantIdentifier The merchant identifier.
     * @param allowedCardAuthMethods List of allowed card authentication methods.
     * @param allowedCardNetworks List of allowed card networks.
     * @param billingAddressRequired Indicates whether billing address is required.
     * @param billingAddressParameters Detailed parameters for the billing address.
     * @param shippingAddressRequired Indicates whether shipping address is required.
     * @param allowedShippingCountryCodes List of country codes allowed for shipping.
     * @param shippingAddressParameters Detailed parameters for the shipping address.
     * @param emailRequired Indicates whether an email address is required.
     * @param phoneNumberRequired Indicates whether a phone number is required.
     * @return Typed object representing the Google Pay payment request.
     */
    fun createGooglePayRequest(
        amount: BigDecimal,
        amountLabel: String,
        countryCode: String,
        currencyCode: String,
        merchantName: String? = null,
        merchantIdentifier: String,
        allowedCardAuthMethods: List<String> = MobileSDKConstants.GooglePayConfig.ALLOWED_CARD_AUTH_METHODS,
        allowedCardNetworks: List<String> = MobileSDKConstants.GooglePayConfig.ALLOWED_CARD_NETWORKS,
        billingAddressRequired: Boolean = true,
        billingAddressParameters: GooglePayBillingAddressParameters? = null,
        shippingAddressRequired: Boolean = false,
        allowedShippingCountryCodes: List<String>? = null,
        shippingAddressParameters: GooglePayShippingAddressParameters? = null,
        emailRequired: Boolean = false,
        phoneNumberRequired: Boolean = false
    ): GooglePayPaymentDataRequest {
        val resolvedBillingAddressParameters =
            if (billingAddressRequired) {
                billingAddressParameters ?: GooglePayBillingAddressParameters(
                    phoneNumberRequired = phoneNumberRequired
                )
            } else {
                null
            }

        val cardParameters = GooglePayCardParameters(
            allowedAuthMethods = allowedCardAuthMethods,
            allowedCardNetworks = allowedCardNetworks,
            billingAddressRequired = billingAddressRequired,
            billingAddressParameters = resolvedBillingAddressParameters
        )

        val cardPaymentMethod = GooglePayCardPaymentMethod(
            parameters = cardParameters,
            tokenizationSpecification = GooglePayPaymentMethodTokenizationSpecification(
                parameters = GooglePayPaymentMethodTokenizationParameters(
                    gatewayMerchantId = merchantIdentifier
                )
            )
        )

        val shippingParams =
            if (shippingAddressRequired) {
                val allowedCountryCodes =
                    shippingAddressParameters?.allowedCountryCodes ?: allowedShippingCountryCodes
                val resolvedPhoneNumberRequired =
                    shippingAddressParameters?.phoneNumberRequired ?: phoneNumberRequired
                val resolvedFormat = shippingAddressParameters?.format

                GooglePayShippingAddressParameters(
                    allowedCountryCodes = allowedCountryCodes,
                    phoneNumberRequired = resolvedPhoneNumberRequired,
                    format = resolvedFormat
                )
            } else {
                null
            }

        return GooglePayPaymentDataRequest(
            emailRequired = emailRequired,
            shippingAddressRequired = shippingAddressRequired,
            allowedPaymentMethods = listOf(cardPaymentMethod),
            transactionInfo = GooglePayTransactionInfo(
                totalPrice = formatAmountForGooglePay(amount),
                totalPriceLabel = amountLabel,
                countryCode = countryCode.uppercase(),
                currencyCode = currencyCode.uppercase()
            ),
            merchantInfo = GooglePayMerchantInfo(
                merchantName = merchantName ?: "",
                merchantId = merchantIdentifier
            ),
            shippingAddressParameters = shippingParams
        )
    }

    fun formatAmountForGooglePay(amount: BigDecimal): String =
        amount.setScale(2, RoundingMode.HALF_UP).toPlainString()

    /**
     * Create a request to check if the user is ready to pay with Google Pay.
     *
     * @param allowedCardAuthMethods List of allowed card authentication methods.
     * @param allowedCardNetworks List of allowed card networks.
     * @param billingAddressRequired Indicates whether billing address is required.
     * @return Typed object representing the readiness to pay request.
     */
    fun createIsReadyToPayRequest(
        allowedCardAuthMethods: List<String> = MobileSDKConstants.GooglePayConfig.ALLOWED_CARD_AUTH_METHODS,
        allowedCardNetworks: List<String> = MobileSDKConstants.GooglePayConfig.ALLOWED_CARD_NETWORKS,
        billingAddressRequired: Boolean = true,
        billingAddressParameters: GooglePayBillingAddressParameters? = null,
        phoneNumberRequired: Boolean = true
    ): GooglePayIsReadyToPayRequest {
        val resolvedBillingAddressParameters =
            if (billingAddressRequired) {
                billingAddressParameters
                    ?: GooglePayBillingAddressParameters(phoneNumberRequired = phoneNumberRequired)
            } else {
                null
            }

        val cardParameters = GooglePayCardParameters(
            allowedAuthMethods = allowedCardAuthMethods,
            allowedCardNetworks = allowedCardNetworks,
            billingAddressRequired = billingAddressRequired,
            billingAddressParameters = resolvedBillingAddressParameters
        )

        val cardPaymentMethod = GooglePayCardPaymentMethod(
            parameters = cardParameters,
            tokenizationSpecification = null
        )

        return GooglePayIsReadyToPayRequest(
            allowedPaymentMethods = listOf(cardPaymentMethod)
        )
    }

    /**
     * Parses the payment data returned from the Google Pay API into a [GooglePayPaymentResponse].
     *
     * @param paymentData The [PaymentData] object returned by Google Pay.
     * @return A [GooglePayPaymentResponse] containing the structured payment details.
     */
    internal fun parsePaymentResponse(paymentData: PaymentData): GooglePayPaymentResponse {
        return GooglePayPaymentResponse.fromJson(paymentData.toJson())
    }
}