package com.paydock.feature.paypal.checkout.presentation.viewmodel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.mapper.mapToPayPalEnv
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.paypal.checkout.presentation.state.PayPalWebCheckoutState
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.paypalwebpayments.PayPalPresentAuthChallengeResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
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
 * @property savedStateHandle Handle for persisting state across process death.
 */
internal class PayPalWebCheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
    dispatchers: DispatchersProvider,
    private val coreConfigProvider: (clientId: String) -> CoreConfig = { clientId ->
        CoreConfig(
            clientId = clientId,
            environment = MobileSDK.getInstance().environment.mapToPayPalEnv()
        )
    },
    private val clientProvider: (
        AppCompatActivity,
        CoreConfig,
        String
    ) -> PayPalWebCheckoutClient = { activity, coreConfig, returnUrl ->
        PayPalWebCheckoutClient(activity, coreConfig, returnUrl)
    }
) : BaseViewModel(dispatchers) {

    private val _checkoutState = MutableStateFlow<PayPalWebCheckoutState>(PayPalWebCheckoutState.Idle)
    val checkoutState: StateFlow<PayPalWebCheckoutState> = _checkoutState.asStateFlow()

    private var paypalClient: PayPalWebCheckoutClient? = null

    // Auth state returned after presenting the auth challenge; required to finish the flow
    // Persisted in SavedStateHandle to survive process death with "Don't keep activities"
    private var authState: String?
        get() = savedStateHandle[KEY_AUTH_STATE]
        set(value) { savedStateHandle[KEY_AUTH_STATE] = value }

    // Client configuration persisted to recreate the client if lost due to process death
    private var clientId: String?
        get() = savedStateHandle[KEY_CLIENT_ID]
        set(value) { savedStateHandle[KEY_CLIENT_ID] = value }

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
            // Persist client configuration for potential recreation after process death
            this@PayPalWebCheckoutViewModel.clientId = clientId
            this@PayPalWebCheckoutViewModel.orderId = orderId

            val coreConfig = coreConfigProvider(clientId)
            val returnUrl = MobileSDKConstants.PayPalConfig.URL_SCHEME
            paypalClient = clientProvider(activity, coreConfig, returnUrl)
            val webCheckoutRequest = PayPalWebCheckoutRequest(orderId, fundingSource)
            when (val present = paypalClient?.start(activity, webCheckoutRequest)) {
                is PayPalPresentAuthChallengeResult.Success -> {
                    authState = present.authState
                }
                is PayPalPresentAuthChallengeResult.Failure -> {
                    _checkoutState.value = PayPalWebCheckoutState.Failure(present.error)
                }
                else -> {
                    _checkoutState.value = PayPalWebCheckoutState.Canceled
                }
            }
        }
    }

    /**
     * Handles the deep link result from the PayPal authentication challenge.
     *
     * Extracts the `paymentMethodId` (orderId) and `payerId` on success and emits [PayPalWebCheckoutState.Success].
     *
     * If the authState is null (e.g., process was killed with "Don't keep activities"),
     * the flow is treated as canceled to allow the Activity to recover gracefully.
     *
     * If the paypalClient is lost due to process death, it will be recreated using persisted configuration.
     *
     * @param activity The hosting activity (required to recreate the client if lost).
     */
    fun handleDeeplinkResult(activity: AppCompatActivity, intent: Intent) {
        val state = authState
        if (state == null) {
            // Process was killed and authState lost - treat as cancellation for graceful recovery
            _checkoutState.value = PayPalWebCheckoutState.Canceled
            return
        }

        // Recreate client if lost due to process death
        if (paypalClient == null) {
            val savedClientId = clientId
            val savedOrderId = orderId
            if (savedClientId != null && savedOrderId != null) {
                val coreConfig = coreConfigProvider(savedClientId)
                val returnUrl = MobileSDKConstants.PayPalConfig.URL_SCHEME
                paypalClient = clientProvider(activity, coreConfig, returnUrl)
            }
        }

        val result = paypalClient?.finishStart(intent, state) ?: PayPalWebCheckoutFinishStartResult.NoResult
        when (result) {
            is PayPalWebCheckoutFinishStartResult.Success -> {
                // Extract values required for capture. The SDK provides them on the success result.
                // Names may vary by SDK version; adapt if the properties differ.
                val paymentMethodId = result.orderId ?: ""
                val payerId = result.payerId ?: ""
                authState = null
                _checkoutState.value = PayPalWebCheckoutState.Success(
                    paymentMethodId = paymentMethodId,
                    payerId = payerId
                )
            }
            is PayPalWebCheckoutFinishStartResult.Canceled -> {
                authState = null
                _checkoutState.value = PayPalWebCheckoutState.Canceled
            }
            is PayPalWebCheckoutFinishStartResult.Failure -> {
                authState = null
                _checkoutState.value = PayPalWebCheckoutState.Failure(result.error)
            }
            is PayPalWebCheckoutFinishStartResult.NoResult -> {
                _checkoutState.value = PayPalWebCheckoutState.Canceled
            }
        }
    }

    /**
     * Clears the PayPal client and authentication state when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        authState = null
        paypalClient = null
    }

    private companion object {
        const val KEY_AUTH_STATE: String = "paypal.checkout.auth_state"
        const val KEY_CLIENT_ID: String = "paypal.checkout.client_id"
        const val KEY_ORDER_ID: String = "paypal.checkout.order_id"
    }
}
