package com.paydock.feature.googlepay.domain.model.integration

/**
 * Represents the configuration for the Google Pay widget.
 *
 * This configuration includes the typed request models for determining readiness to pay and
 * the actual payment request. These are serialized to JSON only at the Google Pay SDK boundary.
 *
 * @property accessToken The access token required for authenticating API requests to create payment tokens.
 * @property serviceId The service ID for Google Pay (SERVICE_ID_GOOGLE_PAY).
 * @property isReadyToPayRequest [GooglePayIsReadyToPayRequest] containing the parameters for checking
 * readiness to pay with Google Pay.
 * @property paymentRequest [GooglePayPaymentDataRequest] containing the full payment request details.
 */
data class GooglePayWidgetConfig(
    val accessToken: String,
    val serviceId: String,
    val isReadyToPayRequest: GooglePayIsReadyToPayRequest,
    val paymentRequest: GooglePayPaymentDataRequest
)