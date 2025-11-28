package com.paydock.feature.paypal.vault.presentation.viewmodel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.paypal.core.presentation.PayPalWebClientManager
import com.paydock.feature.paypal.vault.presentation.state.PayPalWebVaultState
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
 * Uses [PayPalWebClientManager] for centralized client lifecycle management.
 *
 * @property savedStateHandle Handle for persisting state across process death.
 * @property dispatchers The dispatchers provider for coroutine context switching.
 * @property clientManager Manages PayPal Web Client lifecycle and configuration.
 */
internal class PayPalWebVaultViewModel(
    private val savedStateHandle: SavedStateHandle,
    dispatchers: DispatchersProvider,
    private val clientManager: PayPalWebClientManager = PayPalWebClientManager(
        savedStateHandle,
        MobileSDKConstants.PayPalVaultConfig.URL_SCHEME
    )
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the vault result state
    private val _vaultResult = MutableStateFlow<PayPalWebVaultState>(PayPalWebVaultState.Idle)

    // Exposes a read-only state flow for observing the vault result state changes
    val vaultResult: StateFlow<PayPalWebVaultState> = _vaultResult.asStateFlow()

    // Persist setupToken for potential use after process death
    private var setupToken: String?
        get() = savedStateHandle[KEY_SETUP_TOKEN]
        set(value) { savedStateHandle[KEY_SETUP_TOKEN] = value }

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
            // Persist setupToken for potential use after process death
            this@PayPalWebVaultViewModel.setupToken = setupToken

            // Get or create PayPal client
            val paypalClient = clientManager.getOrCreateClient(activity, clientId)
            val request = PayPalWebVaultRequest(setupToken)

            when (val presentResult = paypalClient?.vault(activity, request)) {
                is PayPalPresentAuthChallengeResult.Success -> {
                    // Auth state is stored internally by SDK - no action needed
                }
                is PayPalPresentAuthChallengeResult.Failure -> {
                    _vaultResult.value = PayPalWebVaultState.Failure(presentResult.error)
                }
                null -> {
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
     * If the paypalClient is null (process was killed), it will be recreated using persisted config.
     *
     * When "do not keep activities" is enabled, the PayPal SDK may return NoResult even though
     * the intent contains success data. This method includes fallback logic to manually parse
     * the intent data in such cases.
     *
     * @param activity The activity handling the deep link (needed to recreate client if necessary).
     * @param intent The Intent received from the PayPal redirect or deeplink.
     */
    fun handleDeeplinkResult(activity: AppCompatActivity, intent: Intent) {
        // Get or recreate client if lost due to process death
        val paypalClient = clientManager.getOrCreateClient(activity)

        val result =
            paypalClient?.finishVault(intent) ?: PayPalWebCheckoutFinishVaultResult.NoResult
        when (result) {
            is PayPalWebCheckoutFinishVaultResult.Canceled -> {
                _vaultResult.value = PayPalWebVaultState.Canceled
            }
            is PayPalWebCheckoutFinishVaultResult.Failure -> {
                _vaultResult.value = PayPalWebVaultState.Failure(result.error)
            }
            is PayPalWebCheckoutFinishVaultResult.Success -> {
                val approvalSessionId = result.approvalSessionId
                _vaultResult.value = PayPalWebVaultState.Success(approvalSessionId)
            }
            is PayPalWebCheckoutFinishVaultResult.NoResult -> {
                // Fallback: When "do not keep activities" is enabled, the SDK may return NoResult
                // even though the intent contains success data. Manually parse the intent to check.
                val approvalSessionId = extractApprovalSessionIdFromIntent(intent)
                if (approvalSessionId != null) {
                    _vaultResult.value = PayPalWebVaultState.Success(approvalSessionId)
                } else {
                    // Treat as cancellation so the Activity can close cleanly
                    _vaultResult.value = PayPalWebVaultState.Canceled
                }
            }
        }
    }

    /**
     * Extracts the approval session ID from the intent data URI as a fallback when the SDK
     * returns NoResult but the intent contains success data.
     *
     * This handles the edge case where "do not keep activities" is enabled and the activity
     * is recreated, causing the SDK to lose internal state.
     *
     * @param intent The Intent containing the deep link data.
     * @return The approval session ID if found in the success URL, null otherwise.
     */
    private fun extractApprovalSessionIdFromIntent(intent: Intent): String? {
        val dataUri = intent.data ?: return null
        val expectedPath = "/success"

        // Check if the URI matches the success path
        if (dataUri.path != expectedPath) {
            return null
        }

        // Extract approval_session_id from query parameters
        return dataUri.getQueryParameter("approval_session_id")
    }

    /**
     * Cleans up the resources used by the PayPal vault process when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        clientManager.clear()
    }

    private companion object {
        const val KEY_SETUP_TOKEN: String = "paypal.vault.setup_token"
    }
}
