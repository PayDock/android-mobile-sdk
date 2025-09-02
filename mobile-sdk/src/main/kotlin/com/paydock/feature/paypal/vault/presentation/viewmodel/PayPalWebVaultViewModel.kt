package com.paydock.feature.paypal.vault.presentation.viewmodel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.mapper.mapToPayPalEnv
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.paypal.vault.presentation.state.PayPalWebVaultState
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.paypalwebpayments.PayPalPresentAuthChallengeResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFinishVaultResult
import com.paypal.android.paypalwebpayments.PayPalWebVaultRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel responsible for managing the PayPal Vault flow through a web checkout.
 *
 * This ViewModel interacts with the PayPal SDK to initiate the PayPal vault process,
 * handle user actions, and observe success, failure, or cancellation events.
 * It maintains a state flow of [PayPalWebVaultState] that observers can use to react to changes
 * in the vault process.
 *
 * @property dispatchers The dispatchers provider for coroutine context switching.
 */
internal class PayPalWebVaultViewModel(
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the vault result state
    private val _vaultResult = MutableStateFlow<PayPalWebVaultState>(PayPalWebVaultState.Idle)

    // Exposes a read-only state flow for observing the vault result state changes
    val vaultResult: StateFlow<PayPalWebVaultState> = _vaultResult.asStateFlow()

    // Client instance for interacting with PayPal Web Checkout
    private var paypalClient: PayPalWebCheckoutClient? = null

    // Auth state returned after presenting the auth challenge; required to finish the flow
    private var authState: String? = null

    /**
     * Initiates the PayPal vault process by configuring the [PayPalWebCheckoutClient] with the
     * provided activity, client ID, and setup token.
     *
     * @param activity The activity from which the PayPal vault process is initiated.
     * @param clientId The PayPal client ID to authenticate the vault request.
     * @param setupToken The setup token provided by the backend to initiate the PayPal vault process.
     */
    fun initiatePayPalVault(activity: AppCompatActivity, clientId: String, setupToken: String) {
        launchOnMain {
            val request = PayPalWebVaultRequest(setupToken)
            val coreConfig = CoreConfig(
                clientId = clientId,
                environment = MobileSDK.getInstance().environment.mapToPayPalEnv()
            )
            // Initialize PayPal client and start vault flow
            paypalClient = PayPalWebCheckoutClient(activity, coreConfig, MobileSDKConstants.PayPalVaultConfig.URL_SCHEME)
            val presentResult = paypalClient?.vault(activity, request)
            when (presentResult) {
                is PayPalPresentAuthChallengeResult.Success -> {
                    // Save auth state to complete later
                    authState = presentResult.authState
                }
                is PayPalPresentAuthChallengeResult.Failure -> {
                    _vaultResult.value = PayPalWebVaultState.Failure(
                        presentResult.error
                    )
                }

                else -> {
                    _vaultResult.value = PayPalWebVaultState.Canceled
                }
            }
        }
    }

    /**
     * Handles the result from the PayPal redirect or deeplink Intent.
     *
     * This method should be called from the hosting Activity when the app receives the
     * PayPal redirect or deeplink Intent. It completes the vault flow and updates the
     * UI state accordingly based on the result.
     *
     * @param intent The Intent received from the PayPal redirect or deeplink.
     */
    fun handleDeeplinkResult(intent: Intent) {
        val state = authState
        if (state == null) return

        kotlin.runCatching {
            paypalClient?.finishVault(intent, state)
        }.onSuccess { finishResult ->
            when (finishResult) {
                is PayPalWebCheckoutFinishVaultResult.Canceled -> {
                    authState = null
                    _vaultResult.value = PayPalWebVaultState.Canceled
                }
                is PayPalWebCheckoutFinishVaultResult.Failure -> {
                    authState = null
                    _vaultResult.value = PayPalWebVaultState.Failure(finishResult.error)
                }
                is PayPalWebCheckoutFinishVaultResult.Success -> {
                    val approvalSessionId = finishResult.approvalSessionId
                    authState = null
                    _vaultResult.value = PayPalWebVaultState.Success(approvalSessionId)
                }
                is PayPalWebCheckoutFinishVaultResult.NoResult -> {
                    // Treat as cancellation so the Activity can close cleanly
                    _vaultResult.value = PayPalWebVaultState.Canceled
                }
                else -> {
                    // Fallback in case of unknown result type
                    _vaultResult.value = PayPalWebVaultState.Canceled
                }
            }
        }.onFailure {
            // Fallback in case of unknown result type
            _vaultResult.value = PayPalWebVaultState.Canceled
        }
    }

    /**
     * Cleans up the resources used by the PayPal vault process when the ViewModel is cleared.
     *
     * This removes any observers from the PayPal client.
     */
    override fun onCleared() {
        super.onCleared()
        authState = null
        paypalClient = null
    }
}
