package com.paydock.feature.googlepay.domain.model

import org.json.JSONObject

/**
 * Represents the configuration for the Google Pay widget.
 *
 * This configuration includes the necessary JSON objects for determining readiness to pay and
 * the actual payment request.
 *
 * @property isReadyToPayRequest A [JSONObject] containing the parameters for checking
 * readiness to pay with Google Pay. This typically includes information about supported payment
 * methods and allowed networks.
 * @property paymentRequest A [JSONObject] containing the full payment request details. This
 * includes information about the transaction, such as the total price, currency, and line items,
 * as well as supported payment methods and billing/shipping address requirements.
 */
data class GooglePayWidgetConfig(
    val isReadyToPayRequest: JSONObject,
    val paymentRequest: JSONObject
)