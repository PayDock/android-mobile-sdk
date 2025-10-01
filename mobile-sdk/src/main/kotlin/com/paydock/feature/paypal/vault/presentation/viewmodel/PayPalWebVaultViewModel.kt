package com.paydock.feature.paypal.vault.presentation.viewmodel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
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
 * @property savedStateHandle Handle for persisting state across process death.
 * @property dispatchers The dispatchers provider for coroutine context switching.
 */
internal class PayPalWebVaultViewModel(
    private val savedStateHandle: SavedStateHandle,
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the vault result state
    private val _vaultResult = MutableStateFlow<PayPalWebVaultState>(PayPalWebVaultState.Idle)

    // Exposes a read-only state flow for observing the vault result state changes
    val vaultResult: StateFlow<PayPalWebVaultState> = _vaultResult.asStateFlow()

    // Client instance for interacting with PayPal Web Checkout
    private var paypalClient: PayPalWebCheckoutClient? = null

    // Auth state returned after presenting the auth challenge; required to finish the flow
    // Persisted in SavedStateHandle to survive process death with "Don't keep activities"
    private var authState: String?
        get() = savedStateHandle[KEY_AUTH_STATE]
        set(value) { savedStateHandle[KEY_AUTH_STATE] = value }

    // Store client ID and setup token for recreating client if needed
    private var clientId: String?
        get() = savedStateHandle[KEY_CLIENT_ID]
        set(value) { savedStateHandle[KEY_CLIENT_ID] = value }

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
            // Persist for potential recreation
            this@PayPalWebVaultViewModel.clientId = clientId
            this@PayPalWebVaultViewModel.setupToken = setupToken

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
     * If the authState is null (e.g., process was killed with "Don't keep activities"),
     * the flow is treated as canceled to allow the Activity to recover gracefully.
     *
     * If the paypalClient is null (process was killed), it will be recreated using persisted config.
     *
     * @param activity The activity handling the deep link (needed to recreate client if necessary).
     * @param intent The Intent received from the PayPal redirect or deeplink.
     */
    fun handleDeeplinkResult(activity: AppCompatActivity, intent: Intent) {
        val state = authState
        if (state == null) {
            // Process was killed and authState lost - treat as cancellation for graceful recovery
            _vaultResult.value = PayPalWebVaultState.Canceled
            return
        }

        // Recreate client if it was lost due to process death
        if (paypalClient == null) {
            val savedClientId = clientId
            val savedSetupToken = setupToken
            if (savedClientId != null && savedSetupToken != null) {
                val coreConfig = CoreConfig(
                    clientId = savedClientId,
                    environment = MobileSDK.getInstance().environment.mapToPayPalEnv()
                )
                paypalClient = PayPalWebCheckoutClient(activity, coreConfig, MobileSDKConstants.PayPalVaultConfig.URL_SCHEME)
            }
        }

        val result =
            paypalClient?.finishVault(intent, state) ?: PayPalWebCheckoutFinishVaultResult.NoResult
        when (result) {
            is PayPalWebCheckoutFinishVaultResult.Canceled -> {
                authState = null
                _vaultResult.value = PayPalWebVaultState.Canceled
            }
            is PayPalWebCheckoutFinishVaultResult.Failure -> {
                authState = null
                _vaultResult.value = PayPalWebVaultState.Failure(result.error)
            }
            is PayPalWebCheckoutFinishVaultResult.Success -> {
                val approvalSessionId = result.approvalSessionId
                authState = null
                _vaultResult.value = PayPalWebVaultState.Success(approvalSessionId)
            }
            is PayPalWebCheckoutFinishVaultResult.NoResult -> {
                // Treat as cancellation so the Activity can close cleanly
                _vaultResult.value = PayPalWebVaultState.Canceled
            }
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

    private companion object {
        const val KEY_AUTH_STATE: String = "paypal.vault.auth_state"
        const val KEY_CLIENT_ID: String = "paypal.vault.client_id"
        const val KEY_SETUP_TOKEN: String = "paypal.vault.setup_token"
    }
}
