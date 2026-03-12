package com.paydock.feature.zip.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.domain.error.exceptions.ZipException
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.zip.domain.model.ZipCallbackData
import com.paydock.feature.zip.domain.model.ZipStatus
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.domain.usecase.CreateZipPaymentSourceTokenUseCase
import com.paydock.feature.zip.domain.usecase.InitializeZipCheckoutUseCase
import com.paydock.feature.zip.presentation.state.ZipUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing the state and operations of the Zip payment widget.
 *
 * This ViewModel handles the Zip checkout flow, including initializing the checkout session,
 * processing the Zip callback, and creating the payment source token.
 *
 * @param config The configuration for the Zip widget.
 * @param initializeZipCheckoutUseCase The use case for initializing the Zip checkout.
 * @param createZipPaymentSourceTokenUseCase The use case for creating the payment source token.
 * @param dispatchers The provider for coroutine dispatchers.
 * @param savedStateHandle Handle for persisting state across process death.
 */
internal class ZipViewModel(
    private val config: ZipWidgetConfig,
    private val initializeZipCheckoutUseCase: InitializeZipCheckoutUseCase,
    private val createZipPaymentSourceTokenUseCase: CreateZipPaymentSourceTokenUseCase,
    dispatchers: DispatchersProvider,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel(dispatchers) {

    /**
     * Holds the current UI state for the Zip widget.
     */
    private val _stateFlow: MutableStateFlow<ZipUIState> = MutableStateFlow(ZipUIState.Idle)
    val stateFlow: StateFlow<ZipUIState> = _stateFlow.asStateFlow()

    /**
     * Stores the checkout token after successful initialization.
     * Persisted in SavedStateHandle to survive process death.
     */
    private fun getCheckoutToken(): String? = savedStateHandle[KEY_CHECKOUT_TOKEN]

    private fun setCheckoutToken(token: String?) {
        savedStateHandle[KEY_CHECKOUT_TOKEN] = token
    }

    /**
     * Resets the UI state to idle.
     */
    fun resetResultState() {
        setCheckoutToken(null)
        updateState(ZipUIState.Idle)
    }

    /**
     * Initiates the Zip checkout flow.
     *
     * This method initializes the checkout session and updates the UI state accordingly.
     * On success, it provides the checkout URL and token to launch the Zip webview.
     * On failure, it updates the UI state with the error.
     */
    fun initializeCheckout() {
        launchOnIO {
            updateState(ZipUIState.Loading)

            initializeZipCheckoutUseCase(config.accessToken, config)
                .onSuccess { (checkoutUrl, checkoutToken) ->
                    setCheckoutToken(checkoutToken)
                    updateState(ZipUIState.LaunchCheckout(checkoutUrl, checkoutToken))
                }
                .onFailure { error ->
                    error.safeCastAs<SdkException>()
                        ?.let { updateState(ZipUIState.Error(it)) }
                        ?: updateState(ZipUIState.Error(ZipException.UnknownException()))
                }
        }
    }

    /**
     * Handles the callback from the Zip webview after user completes the checkout.
     *
     * Based on the callback status, this method either proceeds to create a payment source token
     * (on approval) or updates the UI state with the appropriate error.
     *
     * @param callbackData The callback data from the Zip webview.
     */
    fun handleZipCallback(callbackData: ZipCallbackData) {
        when (callbackData.status) {
            ZipStatus.APPROVED -> handleApprovedCallback()
            ZipStatus.DECLINED -> updateState(
                ZipUIState.Error(ZipException.DeclinedException(callbackData.checkoutId))
            )

            ZipStatus.CANCELLED -> updateState(
                ZipUIState.Error(ZipException.CancellationException(callbackData.checkoutId))
            )

            ZipStatus.REFERRED -> updateState(
                ZipUIState.Error(ZipException.ReferredException(callbackData.checkoutId))
            )

            ZipStatus.UNEXPECTED, ZipStatus.UNEXPECTED_ERROR -> updateState(
                ZipUIState.Error(
                    ZipException.UnexpectedStatusException(
                        status = callbackData.status.value,
                        checkoutId = callbackData.checkoutId
                    )
                )
            )
        }
    }

    /**
     * Handles the approved callback by creating a payment source token.
     */
    private fun handleApprovedCallback() {
        val checkoutToken = getCheckoutToken()
        if (checkoutToken == null) {
            updateState(ZipUIState.Error(ZipException.UnknownException(MobileSDKConstants.ZipConfig.Errors.ZIP_ERROR)))
            return
        }

        launchOnIO {
            updateState(ZipUIState.Loading)

            createZipPaymentSourceTokenUseCase(
                accessToken = config.accessToken,
                checkoutToken = checkoutToken,
                gatewayId = config.gatewayId
            )
                .onSuccess { token ->
                    updateState(ZipUIState.Success(token))
                }
                .onFailure { error ->
                    error.safeCastAs<SdkException>()
                        ?.let { updateState(ZipUIState.Error(it)) }
                        ?: updateState(ZipUIState.Error(ZipException.UnknownException()))
                }
        }
    }

    /**
     * Handles webview failure during the Zip checkout flow.
     *
     * @param errorCode The error code from the webview, if available.
     * @param errorMessage The error message from the webview.
     */
    fun handleWebViewError(errorCode: Int?, errorMessage: String) {
        updateState(ZipUIState.Error(ZipException.WebViewException(errorCode, errorMessage)))
    }

    /**
     * Handles user cancellation of the Zip checkout (e.g., dismissing the sheet).
     */
    fun handleUserCancellation() {
        updateState(ZipUIState.Error(ZipException.CancellationException()))
    }

    /**
     * Updates the UI state to a new value.
     *
     * @param newState The new state to set for the UI.
     */
    private fun updateState(newState: ZipUIState) {
        _stateFlow.value = newState
    }

    private companion object {
        const val KEY_CHECKOUT_TOKEN = "zip.checkout_token"
    }
}
