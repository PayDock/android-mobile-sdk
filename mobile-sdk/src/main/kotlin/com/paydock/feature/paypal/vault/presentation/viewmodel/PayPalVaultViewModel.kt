package com.paydock.feature.paypal.vault.presentation.viewmodel

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.paypal.core.data.dto.CreateSetupTokenRequest
import com.paydock.feature.paypal.core.data.dto.PayPalVaultTokenRequest
import com.paydock.feature.paypal.core.domain.usecase.CreatePayPalVaultPaymentTokenUseCase
import com.paydock.feature.paypal.core.domain.usecase.CreateSetupTokenUseCase
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.presentation.state.PayPalVaultUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel responsible for handling the PayPal Vaulting process.
 *
 * @param config Configuration data for PayPal vaulting.
 * @param createSetupTokenUseCase Use case to create a PayPal setup token.
 * @param getPayPalClientIdUseCase Use case to retrieve the PayPal client ID.
 * @param createPayPalVaultPaymentTokenUseCase Use case to create a payment token for the PayPal vault.
 * @param dispatchers Provides dispatcher context for coroutine operations.
 */
@Suppress("LongParameterList")
internal class PayPalVaultViewModel(
    private val config: PayPalVaultConfig,
    private val createSetupTokenUseCase: CreateSetupTokenUseCase,
    private val getPayPalClientIdUseCase: GetPayPalClientIdUseCase,
    private val createPayPalVaultPaymentTokenUseCase: CreatePayPalVaultPaymentTokenUseCase,
    dispatchers: DispatchersProvider,
) : BaseViewModel(dispatchers) {

    // Internal mutable state flow to track UI state.
    private val _stateFlow: MutableStateFlow<PayPalVaultUIState> =
        MutableStateFlow(PayPalVaultUIState.Idle)

    // Public state flow for observing the current UI state.
    val stateFlow: StateFlow<PayPalVaultUIState> = _stateFlow.asStateFlow()

    // Nullable setup token, which is reset as needed during state transitions.
    private var setupToken: String? = null

    /**
     * Resets the setup token and sets the state to [PayPalVaultUIState.Idle].
     */
    fun resetResultState() {
        setupToken = null
        updateState(PayPalVaultUIState.Idle)
    }

    /**
     * Updates the UI state to the given [newState].
     *
     * @param newState The new state to set for the UI.
     */
    private fun updateState(newState: PayPalVaultUIState) {
        _stateFlow.value = newState
    }

    /**
     * Creates a PayPal setup token by invoking [CreateSetupTokenUseCase].
     * - On success, updates the state and proceeds to retrieve the PayPal client ID.
     * - On failure, updates the error state with [PayPalVaultUIState.Error].
     */
    fun createPayPalSetupToken() {
        resetResultState()
        updateState(PayPalVaultUIState.Loading)
        launchOnIO {
            val request = CreateSetupTokenRequest(
                gatewayId = config.gatewayId
            )
            createSetupTokenUseCase(config.accessToken, request)
                .onSuccess { retrievedSetupToken ->
                    setupToken = retrievedSetupToken
                    getPayPalClientId(retrievedSetupToken)
                }
                .onFailure { error ->
                    error.safeCastAs<SdkException>()
                        ?.let { updateState(PayPalVaultUIState.Error(it)) }
                }
        }
    }

    /**
     * Retrieves the PayPal client ID by invoking [GetPayPalClientIdUseCase].
     * - On success, updates the state to [PayPalVaultUIState.LaunchIntent] with the client ID and setup token.
     * - On failure, updates the error state with [PayPalVaultUIState.Error].
     *
     * @param setupToken The setup token required for launching the PayPal intent.
     */
    private fun getPayPalClientId(setupToken: String) {
        launchOnIO {
            getPayPalClientIdUseCase(config.accessToken, config.gatewayId)
                .onSuccess { clientId ->
                    updateState(
                        PayPalVaultUIState.LaunchIntent(
                            clientId = clientId,
                            setupToken = setupToken
                        )
                    )
                }
                .onFailure { error ->
                    error.safeCastAs<SdkException>()
                        ?.let { updateState(PayPalVaultUIState.Error(it)) }
                }
        }
    }

    /**
     * Creates a PayPal payment source token by invoking [CreatePayPalVaultPaymentTokenUseCase].
     * - On success, updates the state to [PayPalVaultUIState.Success] with the payment token.
     * - On failure, updates the error state with [PayPalVaultUIState.Error].
     */
    fun createPayPalPaymentSourceToken() {
        updateState(PayPalVaultUIState.Loading)
        launchOnIO {
            setupToken?.let { setupToken ->
                val request =
                    PayPalVaultTokenRequest(
                        gatewayId = config.gatewayId
                    )
                createPayPalVaultPaymentTokenUseCase(config.accessToken, setupToken, request)
                    .onSuccess { tokenDetails ->
                        updateState(PayPalVaultUIState.Success(tokenDetails))
                    }
                    .onFailure { error ->
                        error.safeCastAs<SdkException>()
                            ?.let { updateState(PayPalVaultUIState.Error(it)) }
                    }
            }
        }
    }
}