/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.googlepay.util

import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.paydock.core.ALLOWED_CARD_AUTH_METHODS
import com.paydock.core.ALLOWED_CARD_NETWORKS
import com.paydock.core.BILLING_ADDRESS_FORMAT
import com.paydock.core.CARD_PAYMENT_TYPE
import com.paydock.core.GATEWAY
import com.paydock.core.TOKENIZATION_TYPE
import com.paydock.core.TRANSACTION_PRICE_STATUS
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal

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
     * @param shippingAddressRequired Indicates whether shipping address is required.
     * @param shippingAddressParameters Parameters for shipping address, if required.
     * @return JSON object representing the Google Pay payment request.
     * @see [PaymentDataRequest](https://developers.google.com/pay/api/android/reference/request-objects#PaymentDataRequest)
     */
    fun createGooglePayRequest(
        amount: BigDecimal,
        amountLabel: String,
        countryCode: String,
        currencyCode: String,
        merchantName: String? = null,
        merchantIdentifier: String,
        allowedCardAuthMethods: List<String> = ALLOWED_CARD_AUTH_METHODS,
        allowedCardNetworks: List<String> = ALLOWED_CARD_NETWORKS,
        billingAddressRequired: Boolean = true,
        shippingAddressRequired: Boolean = false,
        shippingAddressParameters: JSONObject? = null
    ): JSONObject {
        return baseRequest().apply {
            put(
                "allowedPaymentMethods",
                JSONArray().put(
                    cardPaymentMethod(
                        merchantIdentifier,
                        JSONArray(allowedCardAuthMethods),
                        JSONArray(allowedCardNetworks),
                        billingAddressRequired
                    )
                )
            )
            put(
                "transactionInfo",
                getTransactionInfo(
                    amount.toPlainString(),
                    amountLabel,
                    countryCode,
                    currencyCode
                )
            )
            put(
                "merchantInfo",
                JSONObject().apply {
                    put("merchantName", merchantName ?: "")
                    put("merchantId", merchantIdentifier)
                }
            )
            put("shippingAddressParameters", shippingAddressParameters)
            put("shippingAddressRequired", shippingAddressRequired)
        }
    }

    /**
     * Create a request to check if the user is ready to pay with Google Pay.
     *
     * @param allowedCardAuthMethods List of allowed card authentication methods.
     * @param allowedCardNetworks List of allowed card networks.
     * @param billingAddressRequired Indicates whether billing address is required.
     * @return JSON object representing the readiness to pay request.
     * @see [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/request-objects#IsReadyToPayRequest)
     */
    @Suppress("SwallowedException")
    fun createIsReadyToPayRequest(
        allowedCardAuthMethods: List<String> = ALLOWED_CARD_AUTH_METHODS,
        allowedCardNetworks: List<String> = ALLOWED_CARD_NETWORKS,
        billingAddressRequired: Boolean = true
    ): JSONObject {
        return baseRequest().apply {
            put(
                "allowedPaymentMethods",
                JSONArray().put(
                    baseCardPaymentMethod(
                        JSONArray(allowedCardAuthMethods),
                        JSONArray(allowedCardNetworks),
                        billingAddressRequired
                    )
                )
            )
        }
    }

    /**
     * Create a Google Pay API base request object with properties used in all requests.
     *
     * @return Google Pay API base request object.
     * @throws JSONException
     */
    private fun baseRequest() = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @return information about the requested payment.
     * @throws JSONException
     * See [TransactionInfo](https://developers.google.com/pay/api/android/reference/request-objects#TransactionInfo)
     */
    @Throws(JSONException::class)
    private fun getTransactionInfo(
        amount: String,
        amountLabel: String,
        countryCode: String,
        currencyCode: String
    ): JSONObject {
        return JSONObject().apply {
            put("totalPrice", amount)
            put("totalPriceLabel", amountLabel)
            put("totalPriceStatus", TRANSACTION_PRICE_STATUS)
            put("countryCode", countryCode.uppercase())
            put("currencyCode", currencyCode.uppercase())
        }
    }

    /**
     * Describe the expected returned payment data for the CARD payment method
     *
     * @return A CARD PaymentMethod describing accepted cards and optional fields.
     * @throws JSONException
     * See [PaymentMethod](https://developers.google.com/pay/api/android/reference/request-objects#PaymentMethod)
     */
    private fun cardPaymentMethod(
        merchantIdentifier: String,
        allowedCardAuthMethods: JSONArray,
        allowedCardNetworks: JSONArray,
        billingAddressRequired: Boolean
    ): JSONObject {
        val cardPaymentMethod =
            baseCardPaymentMethod(
                allowedCardAuthMethods,
                allowedCardNetworks,
                billingAddressRequired
            )
        cardPaymentMethod.put(
            "tokenizationSpecification",
            gatewayTokenizationSpecification(merchantIdentifier)
        )

        return cardPaymentMethod
    }

    /**
     * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
     *
     * The Google Pay API response will return an encrypted payment method capable of being charged
     * by a supported gateway after payer authorization.
     *
     * @return Payment data tokenization for the CARD payment method.
     * @throws JSONException
     * See [PaymentMethodTokenizationSpecification](https://developers.google.com/pay/api/android/reference/request-objects#PaymentMethodTokenizationSpecification)
     */
    private fun gatewayTokenizationSpecification(merchantIdentifier: String): JSONObject {
        return JSONObject().apply {
            put("type", TOKENIZATION_TYPE)
            put(
                "parameters",
                JSONObject(
                    mapOf(
                        "gateway" to GATEWAY,
                        "gatewayMerchantId" to merchantIdentifier
                    )
                )
            )
        }
    }

    /**
     * Describe your app's support for the CARD payment method.
     *
     *
     * The provided properties are applicable to both an IsReadyToPayRequest and a
     * PaymentDataRequest.
     *
     * @return A CARD PaymentMethod object describing accepted cards.
     * @throws JSONException
     * See [PaymentMethod](https://developers.google.com/pay/api/android/reference/request-objects#PaymentMethod)
     */
    // Optionally, you can add billing address/phone number associated with a CARD payment method.
    private fun baseCardPaymentMethod(
        allowedCardAuthMethods: JSONArray,
        allowedCardNetworks: JSONArray,
        billingAddressRequired: Boolean
    ): JSONObject {
        return JSONObject().apply {

            val parameters = JSONObject().apply {
                put("allowedAuthMethods", allowedCardAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
                put("billingAddressRequired", billingAddressRequired)
                put(
                    "billingAddressParameters",
                    JSONObject().apply {
                        put("format", BILLING_ADDRESS_FORMAT)
                    }
                )
            }

            put("type", CARD_PAYMENT_TYPE)
            put("parameters", parameters)
        }
    }
}