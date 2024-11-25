package com.paydock.feature.wallet.presentation.viewmodels

import com.paydock.api.charges.data.dto.CaptureWalletChargeRequest
import com.paydock.api.charges.data.dto.WalletCallbackRequest
import com.paydock.api.charges.domain.model.WalletCallback
import com.paydock.api.charges.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.GetWalletCallbackUseCase
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.charge.domain.model.integration.ChargeResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base ViewModel for handling wallet-related operations, such as capturing wallet transactions.
 *
 * @param captureWalletChargeUseCase The use case responsible for capturing wallet transactions.
 * @param getWalletCallbackUseCase The use case getting wallet callback details.
 * @param dispatchers Provides the coroutine dispatchers for handling asynchronous tasks.
 * @param T The type representing the UI state of the ViewModel.
 */
internal abstract class WalletViewModel<T>(
    private val captureWalletChargeUseCase: CaptureWalletChargeUseCase,
    private val declineWalletChargeUseCase: DeclineWalletChargeUseCase,
    private val getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<T> = MutableStateFlow(this.createInitialState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<T> = _stateFlow.asStateFlow()

    /**
     * Creates the initial state for the UI.
     *
     * @return Initial state of the UI.
     */
    abstract fun createInitialState(): T

    /**
     * Sets the wallet authentication token in the UI state.
     *
     * @param token The wallet authentication token.
     */
    abstract fun setWalletToken(token: String)

    /**
     * Resets the result state, clearing token and error.
     */
    abstract fun resetResultState()

    /**
     * Sets the UI state to indicate loading.
     */
    abstract fun setLoadingState()

    /**
     * Updates the UI state based on the result of a wallet transaction capture.
     *
     * @param result The result of capturing a wallet transaction.
     */
    open fun updateChargeUIState(result: Result<ChargeResponse>) {}

    /**
     * Updates the UI state based on the result of fetching wallet callback data.
     *
     * @param result The result of fetching wallet callback data.
     */
    open fun updateCallbackUIState(result: Result<WalletCallback>) {}

    /**
     * Updates the UI state using the provided update function.
     *
     * @param update Function to modify the current UI state.
     */
    protected fun updateState(update: (T) -> T) {
        _stateFlow.update { state ->
            update(state)
        }
    }

    /**
     * Capture a wallet transaction using the provided wallet token and request.
     *
     * @param walletToken The wallet authentication token.
     * @param request The request object for capturing the wallet transaction.
     */
    protected fun captureWalletTransaction(
        walletToken: String,
        request: CaptureWalletChargeRequest
    ) {
        launchOnIO {
            // Update the UI state to indicate loading
            setLoadingState()
            // Use the captureWalletTransactionUseCase to capture the wallet transaction
            val result: Result<ChargeResponse> =
                captureWalletChargeUseCase(token = walletToken, request = request)
            // Update the UI state with the result
            updateChargeUIState(result)
        }
    }

    /**
     * Decline a wallet transaction using the provided wallet token and chargeId.
     *
     * @param walletToken The wallet authentication token.
     * @param chargeId The chargeId required for the transaction.
     */
    fun declineWalletTransaction(
        walletToken: String,
        chargeId: String
    ) {
        launchOnIO {
            // Update the UI state to indicate loading
            setLoadingState()
            // Use the declineWalletTransactionUseCase to decline the wallet transaction
            val result: Result<ChargeResponse> =
                declineWalletChargeUseCase(token = walletToken, chargeId = chargeId)
            // Update the UI state with the result
            updateChargeUIState(result)
        }
    }

    /**
     * Initiates the process to retrieve the wallet callback data based on the provided parameters.
     *
     * @param walletToken The wallet authentication token.
     * @param request The request object for fetching wallet callback data.
     */
    protected fun getWalletCallback(walletToken: String, request: WalletCallbackRequest) {
        // Get the token from the current state
        launchOnIO {
            // Update the UI state to indicate loading
            setLoadingState()
            // Use the getWalletCallbackUseCase to fetch the wallet callback data
            val result: Result<WalletCallback> =
                getWalletCallbackUseCase(token = walletToken, request = request)

            // Update the UI state with the result
            updateCallbackUIState(result)
        }
    }
}
