package com.paydock.feature.paypal.checkout.presentation.viewmodel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.paypal.checkout.presentation.state.PayPalWebCheckoutState
import com.paydock.feature.paypal.core.presentation.PayPalWebClientManager
import com.paypal.android.paypalwebpayments.PayPalPresentAuthChallengeResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFinishStartResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel that orchestrates the PayPal Web SDK checkout presentation and results.
 *
 * Exposes [checkoutState] so the hosting Activity can react to SDK presentation results,
 * deep link completions, and errors.
 *
 * Uses [PayPalWebClientManager] for centralized client lifecycle management.
 *
 * @property savedStateHandle Handle for persisting state across process death.
 * @property clientManager Manages PayPal Web Client lifecycle and configuration.
 */
internal class PayPalWebCheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
    dispatchers: DispatchersProvider,
    private val clientManager: PayPalWebClientManager = PayPalWebClientManager(
        savedStateHandle,
        MobileSDKConstants.PayPalConfig.URL_SCHEME
    )
) : BaseViewModel(dispatchers) {

    private val _checkoutState = MutableStateFlow<PayPalWebCheckoutState>(PayPalWebCheckoutState.Idle)
    val checkoutState: StateFlow<PayPalWebCheckoutState> = _checkoutState.asStateFlow()

    // Persist orderId for potential use after process death
    private var orderId: String?
        get() = savedStateHandle[KEY_ORDER_ID]
        set(value) { savedStateHandle[KEY_ORDER_ID] = value }

    /**
     * Initializes the PayPal SDK and presents the authentication challenge to the user.
     *
     * @param activity The hosting activity.
     * @param clientId PayPal client id used to configure the SDK.
     * @param orderId PayPal order id to approve.
     * @param fundingSource PayPal funding source to use to launch PayPal.
     */
    fun initiateCheckout(
        activity: AppCompatActivity,
        clientId: String,
        orderId: String,
        fundingSource: PayPalWebCheckoutFundingSource
    ) {
        launchOnMain {
            // Persist orderId for potential use after process death
            this@PayPalWebCheckoutViewModel.orderId = orderId

            // Get or create PayPal client
            val paypalClient = clientManager.getOrCreateClient(activity, clientId)
            val webCheckoutRequest = PayPalWebCheckoutRequest(orderId, fundingSource)

            paypalClient?.start(activity, webCheckoutRequest) { result ->
                when (result) {
                    is PayPalPresentAuthChallengeResult.Success -> {
                        // Auth state is stored internally by SDK - no action needed
                    }
                    is PayPalPresentAuthChallengeResult.Failure -> {
                        _checkoutState.value = PayPalWebCheckoutState.Failure(result.error)
                    }
                }
            }
        }
    }

    /**
     * Handles the deep link result from the PayPal authentication challenge.
     *
     * Extracts the `paymentMethodId` (orderId) and `payerId` on success and emits [PayPalWebCheckoutState.Success].
     *
     * If the paypalClient is lost due to process death, it will be recreated using persisted configuration.
     *
     * When "do not keep activities" is enabled, the PayPal SDK may return NoResult even though
     * the intent contains success data. This method includes fallback logic to manually parse
     * the intent data in such cases.
     *
     * @param activity The hosting activity (required to recreate the client if lost).
     * @param intent The Intent received from the PayPal redirect or deeplink.
     */
    fun handleDeeplinkResult(activity: AppCompatActivity, intent: Intent) {
        // Get or recreate client if lost due to process death
        val paypalClient = clientManager.getOrCreateClient(activity)

        val result = paypalClient?.finishStart(intent) ?: PayPalWebCheckoutFinishStartResult.NoResult
        when (result) {
            is PayPalWebCheckoutFinishStartResult.Success -> {
                // Extract values required for capture. The SDK provides them on the success result.
                val paymentMethodId = result.orderId ?: ""
                val payerId = result.payerId ?: ""
                _checkoutState.value = PayPalWebCheckoutState.Success(
                    paymentMethodId = paymentMethodId,
                    payerId = payerId
                )
            }
            is PayPalWebCheckoutFinishStartResult.Canceled -> {
                _checkoutState.value = PayPalWebCheckoutState.Canceled
            }
            is PayPalWebCheckoutFinishStartResult.Failure -> {
                _checkoutState.value = PayPalWebCheckoutState.Failure(result.error)
            }
            is PayPalWebCheckoutFinishStartResult.NoResult -> {
                // Fallback: When "do not keep activities" is enabled, the SDK may return NoResult
                // even though the intent contains success data. Manually parse the intent to check.
                val checkoutData = extractCheckoutDataFromIntent(intent)
                if (checkoutData != null) {
                    _checkoutState.value = PayPalWebCheckoutState.Success(
                        paymentMethodId = checkoutData.orderId,
                        payerId = checkoutData.payerId
                    )
                } else {
                    _checkoutState.value = PayPalWebCheckoutState.Canceled
                }
            }
        }
    }

    /**
     * Data class to hold extracted checkout data from intent.
     */
    private data class CheckoutData(
        val orderId: String,
        val payerId: String
    )

    /**
     * Extracts the order ID and payer ID from the intent data URI as a fallback when the SDK
     * returns NoResult but the intent contains success data.
     *
     * This handles the edge case where "do not keep activities" is enabled and the activity
     * is recreated, causing the SDK to lose internal state.
     *
     * The expected URL pattern is:
     * {scheme}://paypal-sdk/paypal-checkout?opType=payment&token={orderId}&PayerID={payerId}
     *
     * @param intent The Intent containing the deep link data.
     * @return A [CheckoutData] object with orderId and payerId if found in the success URL, null otherwise.
     */
    private fun extractCheckoutDataFromIntent(intent: Intent): CheckoutData? {
        val dataUri = intent.data ?: return null
        val expectedPath = "/paypal-sdk/paypal-checkout"

        // Check if the URI matches the expected path
        if (dataUri.path != expectedPath) {
            return null
        }

        // Check if opType is payment (success case)
        val opType = dataUri.getQueryParameter("opType")
        if (opType != "payment") {
            return null
        }

        // Extract orderId (from token parameter) and payerId from query parameters
        // Note: PayPal uses PayerID (capital ID) in the URL
        val orderId = dataUri.getQueryParameter("token") ?: return null
        val payerId = dataUri.getQueryParameter("PayerID")
            ?: dataUri.getQueryParameter("payerId") // Fallback to lowercase for robustness
            ?: return null

        return CheckoutData(orderId = orderId, payerId = payerId)
    }

    /**
     * Clears the PayPal client when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        clientManager.clear()
    }

    private companion object {
        const val KEY_ORDER_ID: String = "paypal.checkout.order_id"
    }
}
