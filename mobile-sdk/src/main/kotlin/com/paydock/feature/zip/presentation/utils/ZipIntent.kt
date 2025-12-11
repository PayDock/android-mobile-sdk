package com.paydock.feature.zip.presentation.utils

import android.content.Intent

/**
 * This object defines constants used as keys for Zip-specific extras in an Intent.
 */
private object ZipIntent {
    const val CHECKOUT_URL = "ZIP_CHECKOUT_URL"
    const val CHECKOUT_TOKEN = "ZIP_CHECKOUT_TOKEN"
    const val RESULT_STATUS = "ZIP_RESULT_STATUS"
    const val RESULT_CHECKOUT_ID = "ZIP_RESULT_CHECKOUT_ID"
    const val RESULT_ORDER_ID = "ZIP_RESULT_ORDER_ID"
    const val ERROR_CODE = "ZIP_ERROR_CODE"
    const val ERROR_MESSAGE = "ZIP_ERROR_MESSAGE"
}

/**
 * Extension function to add the Zip checkout URL to an Intent.
 */
internal fun Intent.putCheckoutUrlExtra(url: String): Intent =
    putExtra(ZipIntent.CHECKOUT_URL, url)

/**
 * Extension function to retrieve the Zip checkout URL from an Intent.
 */
internal fun Intent.getCheckoutUrlExtra(): String? =
    getStringExtra(ZipIntent.CHECKOUT_URL)

/**
 * Extension function to add the Zip checkout token to an Intent.
 */
internal fun Intent.putCheckoutTokenExtra(token: String): Intent =
    putExtra(ZipIntent.CHECKOUT_TOKEN, token)

/**
 * Extension function to retrieve the Zip checkout token from an Intent.
 */
internal fun Intent.getCheckoutTokenExtra(): String? =
    getStringExtra(ZipIntent.CHECKOUT_TOKEN)

/**
 * Extension function to add the Zip result status to an Intent.
 */
internal fun Intent.putResultStatusExtra(status: String): Intent =
    putExtra(ZipIntent.RESULT_STATUS, status)

/**
 * Extension function to retrieve the Zip result status from an Intent.
 */
internal fun Intent.getResultStatusExtra(): String? =
    getStringExtra(ZipIntent.RESULT_STATUS)

/**
 * Extension function to add the Zip checkout ID to an Intent.
 */
internal fun Intent.putResultCheckoutIdExtra(checkoutId: String?): Intent =
    apply { checkoutId?.let { putExtra(ZipIntent.RESULT_CHECKOUT_ID, it) } }

/**
 * Extension function to retrieve the Zip checkout ID from an Intent.
 */
internal fun Intent.getResultCheckoutIdExtra(): String? =
    getStringExtra(ZipIntent.RESULT_CHECKOUT_ID)

/**
 * Extension function to add the Zip order ID to an Intent.
 */
internal fun Intent.putResultOrderIdExtra(orderId: String?): Intent =
    apply { orderId?.let { putExtra(ZipIntent.RESULT_ORDER_ID, it) } }

/**
 * Extension function to retrieve the Zip order ID from an Intent.
 */
internal fun Intent.getResultOrderIdExtra(): String? =
    getStringExtra(ZipIntent.RESULT_ORDER_ID)

/**
 * Extension function to add an error code to an Intent.
 */
internal fun Intent.putErrorCodeExtra(code: Int?): Intent =
    apply { code?.let { putExtra(ZipIntent.ERROR_CODE, it) } }

/**
 * Extension function to retrieve an error code from an Intent.
 */
internal fun Intent.getErrorCodeExtra(): Int? =
    if (hasExtra(ZipIntent.ERROR_CODE)) getIntExtra(ZipIntent.ERROR_CODE, -1) else null

/**
 * Extension function to add an error message to an Intent.
 */
internal fun Intent.putErrorMessageExtra(message: String): Intent =
    putExtra(ZipIntent.ERROR_MESSAGE, message)

/**
 * Extension function to retrieve an error message from an Intent.
 */
internal fun Intent.getErrorMessageExtra(): String? =
    getStringExtra(ZipIntent.ERROR_MESSAGE)
