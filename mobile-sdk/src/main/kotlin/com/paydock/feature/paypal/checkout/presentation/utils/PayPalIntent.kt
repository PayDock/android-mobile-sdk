package com.paydock.feature.paypal.checkout.presentation.utils

import android.content.Intent
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource

/**
 * This object defines constants used as keys for PayPal-specific extras in an Intent.
 */
private object PayPalIntent {
    const val CANCELLATION_STATUS = "PAYPAL_CANCELLATION_STATUS" // Key for the cancellation status.
    const val PAYMENT_METHOD_ID = "PAYPAL_PAYMENT_METHOD_ID" // Key for the payment method id (orderId)
    const val PAYER_ID = "PAYPAL_PAYER_ID" // Key for the payer id
    const val CLIENT_ID = "PAYPAL_CLIENT_ID" // Key for PayPal client id used to init web checkout
    const val ORDER_ID = "PAYPAL_ORDER_ID" // Key for PayPal order id used to start web checkout
    const val FUNDING_SOURCE = "PAYPAL_FUNDING_SOURCE" // Key for PayPal funding source used to start web checkout
}

/**
 * Extension function to add the cancellation status to an Intent.
 *
 * @param status The cancellation status to be added.
 * @return The updated Intent with the cancellation status included.
 */
internal fun Intent.putCancellationStatusExtra(status: CancellationStatus): Intent =
    putExtra(PayPalIntent.CANCELLATION_STATUS, status.name)

/**
 * Extension function to retrieve the cancellation status from an Intent.
 *
 * @return The cancellation status if present, or null if parsing fails.
 */
internal fun Intent.getCancellationStatusExtra(): CancellationStatus? = try {
    getStringExtra(PayPalIntent.CANCELLATION_STATUS)?.let { enumValueOf<CancellationStatus>(it) }
} catch (_: Exception) {
    null
}

// Removed legacy callback/decoded URL helpers that were used by the custom WebView flow

/**
 * Extension to add the payment method id (orderId) to an Intent.
 */
internal fun Intent.putPaymentMethodIdExtra(paymentMethodId: String): Intent =
    putExtra(PayPalIntent.PAYMENT_METHOD_ID, paymentMethodId)

/**
 * Extension to retrieve the payment method id (orderId) from an Intent.
 */
internal fun Intent.getPaymentMethodIdExtra(): String? =
    getStringExtra(PayPalIntent.PAYMENT_METHOD_ID)

/**
 * Extension to add the payer id to an Intent.
 */
internal fun Intent.putPayerIdExtra(payerId: String): Intent =
    putExtra(PayPalIntent.PAYER_ID, payerId)

/**
 * Extension to retrieve the payer id from an Intent.
 */
internal fun Intent.getPayerIdExtra(): String? =
    getStringExtra(PayPalIntent.PAYER_ID)

/**
 * Extensions for client id, order id and funding source used to start the checkout flow.
 */
internal fun Intent.putClientIdExtra(clientId: String): Intent =
    putExtra(PayPalIntent.CLIENT_ID, clientId)

internal fun Intent.getClientIdExtra(): String? =
    getStringExtra(PayPalIntent.CLIENT_ID)

internal fun Intent.putOrderIdExtra(orderId: String): Intent =
    putExtra(PayPalIntent.ORDER_ID, orderId)

internal fun Intent.getOrderIdExtra(): String? =
    getStringExtra(PayPalIntent.ORDER_ID)

internal fun Intent.putFundingSourceExtra(fundingSource: PayPalWebCheckoutFundingSource): Intent =
    putExtra(PayPalIntent.FUNDING_SOURCE, fundingSource.name)

internal fun Intent.getFundingSourceExtra(): String? =
    getStringExtra(PayPalIntent.FUNDING_SOURCE)
