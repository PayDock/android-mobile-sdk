package com.paydock.feature.paypal.vault.presentation.viewmodel

import androidx.appcompat.app.AppCompatActivity
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.mapper.mapToPayPalEnv
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.paypal.vault.presentation.state.PayPalWebVaultState
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebVaultListener
import com.paypal.android.paypalwebpayments.PayPalWebVaultRequest
import com.paypal.android.paypalwebpayments.PayPalWebVaultResult
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
) : BaseViewModel(dispatchers), PayPalWebVaultListener {

    // Mutable state flow to hold the vault result state
    private val _vaultResult = MutableStateFlow<PayPalWebVaultState>(PayPalWebVaultState.Idle)

    // Exposes a read-only state flow for observing the vault result state changes
    val vaultResult: StateFlow<PayPalWebVaultState> = _vaultResult.asStateFlow()

    // Client instance for interacting with PayPal Web Checkout
    private var paypalClient: PayPalWebCheckoutClient? = null

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
            // Initialize PayPal client and set the vault listener
            paypalClient = PayPalWebCheckoutClient(activity, coreConfig, MobileSDKConstants.PayPalVaultConfig.URL_SCHEME).apply {
                vaultListener = this@PayPalWebVaultViewModel
                vault(request)
            }
        }
    }

    /**
     * Called when the PayPal vault process is canceled by the user.
     *
     * This updates the [vaultResult] state to [PayPalWebVaultState.Canceled].
     */
    override fun onPayPalWebVaultCanceled() {
        _vaultResult.value = PayPalWebVaultState.Canceled
    }

    /**
     * Called when the PayPal vault process fails due to an error.
     *
     * This updates the [vaultResult] state to [PayPalWebVaultState.Failure] with the
     * provided [PayPalSDKError].
     *
     * @param error The error that caused the vault process to fail.
     */
    override fun onPayPalWebVaultFailure(error: PayPalSDKError) {
        _vaultResult.value = PayPalWebVaultState.Failure(error)
    }

    /**
     * Called when the PayPal vault process succeeds.
     *
     * This updates the [vaultResult] state to [PayPalWebVaultState.Success] with the
     * approval session ID provided in the [PayPalWebVaultResult].
     *
     * @param result The result of the successful PayPal vault process.
     */
    override fun onPayPalWebVaultSuccess(result: PayPalWebVaultResult) {
        _vaultResult.value = PayPalWebVaultState.Success(result.approvalSessionId)
    }

    /**
     * Cleans up the resources used by the PayPal vault process when the ViewModel is cleared.
     *
     * This removes any observers from the PayPal client.
     */
    override fun onCleared() {
        super.onCleared()
        paypalClient?.removeObservers()
    }
}
