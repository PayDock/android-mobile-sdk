package com.paydock.sample.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.feature.card.presentation.model.CardResult
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.threeDS.domain.model.ThreeDSResult
import com.paydock.feature.wallet.domain.model.WalletType
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.core.THREE_DS_CARD_ERROR
import com.paydock.sample.core.THREE_DS_STATUS_ERROR
import com.paydock.sample.core.TOKENISE_CARD_ERROR
import com.paydock.sample.core.US_CURRENCY_CODE
import com.paydock.sample.core.presentation.utils.AccessTokenProvider
import com.paydock.sample.feature.card.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.card.domain.usecase.CaptureCardChargeTokenUseCase
import com.paydock.sample.feature.card.domain.usecase.CreateCardSessionVaultTokenUseCase
import com.paydock.sample.feature.threeDS.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.model.ThreeDSToken
import com.paydock.sample.feature.threeDS.domain.usecase.CreateIntegratedThreeDSTokenUseCase
import com.paydock.sample.feature.wallet.data.api.dto.Customer
import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.data.api.dto.Meta
import com.paydock.sample.feature.wallet.data.api.dto.PaymentSource
import com.paydock.sample.feature.wallet.domain.model.WalletCharge
import com.paydock.sample.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.sample.feature.wallet.domain.usecase.InitiateWalletTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StandaloneCheckoutViewModel @Inject constructor(
    private val accessTokenProvider: AccessTokenProvider,
    private val initiateWalletTransactionUseCase: InitiateWalletTransactionUseCase,
    private val createCardSessionVaultTokenUseCase: CreateCardSessionVaultTokenUseCase,
    private val createIntegratedThreeDSTokenUseCase: CreateIntegratedThreeDSTokenUseCase,
    private val captureCardChargeTokenUseCase: CaptureCardChargeTokenUseCase,
    private val captureWalletChargeUseCase: CaptureWalletChargeUseCase
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<CheckoutUIState> = MutableStateFlow(CheckoutUIState())
    val stateFlow: StateFlow<CheckoutUIState> = _stateFlow

    val accessToken: StateFlow<String> = accessTokenProvider.accessToken

    fun resetResultState() {
        _stateFlow.update { state ->
            state.copy(
                cardToken = null,
                vaultToken = null,
                walletChargeResult = null,
                threeDSToken = null,
                chargeResult = null,
                flyPayResult = null,
                error = null
            )
        }
    }

    fun getWalletToken(walletType: WalletType): (onTokenReceived: (String) -> Unit) -> Unit =
        { onTokenReceived ->
            resetResultState()
            when (walletType) {
                WalletType.AFTER_PAY -> {
                    val request = createAfterpayWalletRequest()
                    initiateWalletTransaction(
                        request = request,
                        callback = onTokenReceived
                    )
                }

                WalletType.GOOGLE -> {
                    val request = createGoogleWalletRequest()
                    initiateWalletTransaction(
                        request = request,
                        callback = onTokenReceived
                    )
                }

                WalletType.FLY_PAY -> {
                    val request = createFlyPayWalletRequest()
                    initiateWalletTransaction(
                        manualCapture = true,
                        request = request,
                        callback = onTokenReceived
                    )
                }

                WalletType.PAY_PAL -> {
                    val request = createPayPalWalletRequest()
                    initiateWalletTransaction(
                        request = request,
                        callback = onTokenReceived
                    )
                }
            }
        }

    private fun createPayPalWalletRequest(): InitiateWalletRequest {
        return InitiateWalletRequest(
            currency = US_CURRENCY_CODE,
            customer = Customer(
                paymentSource = PaymentSource(
                    gatewayId = BuildConfig.GATEWAY_ID_PAY_PAL,
                    walletType = WalletType.PAY_PAL.type
                )
            )
        )
    }

    private fun createFlyPayWalletRequest(): InitiateWalletRequest {
        return InitiateWalletRequest(
            currency = AU_CURRENCY_CODE,
            customer = Customer(
                paymentSource = PaymentSource(
                    gatewayId = BuildConfig.GATEWAY_ID_FLY_PAY,
                    walletType = WalletType.FLY_PAY.type
                )
            )
        )
    }

    private fun createGoogleWalletRequest(): InitiateWalletRequest {
        return InitiateWalletRequest(
            currency = AU_CURRENCY_CODE,
            customer = Customer(
                paymentSource = PaymentSource(
                    gatewayId = BuildConfig.GATEWAY_ID,
                    walletType = WalletType.GOOGLE.type
                )
            )
        )

    }

    private fun createAfterpayWalletRequest(): InitiateWalletRequest {
        return InitiateWalletRequest(
            currency = AU_CURRENCY_CODE,
            customer = Customer(
                email = "david.cameron@paydock.com",
                firstName = "David",
                lastName = "Cameron",
                paymentSource = PaymentSource(
                    gatewayId = BuildConfig.GATEWAY_ID_AFTER_PAY,
                    walletType = WalletType.AFTER_PAY.type,
                    addressLine1 = "asd1",
                    addressLine2 = "asd1",
                    addressLine3 = "asd1",
                    city = "city",
                    state = "state",
                    countryCode = "US",
                    postalCode = "12345",
                )
            ),
            meta = Meta(
                successUrl = "https://paydock-integration.netlify.app/success",
                errorUrl = "https://paydock-integration.netlify.app/error"
            )
        )
    }

    fun createSessionVaultToken(cardToken: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val request = VaultTokenRequest.CreateCardSessionVaultTokenRequest(token = cardToken)
            val result = createCardSessionVaultTokenUseCase(request)
            result.onSuccess { vaultToken ->
                _stateFlow.update { state ->
                    state.copy(vaultToken = vaultToken)
                }
                createIntegrated3dsToken(vaultToken)
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        isLoading = false,
                        error = it.message ?: TOKENISE_CARD_ERROR
                    )
                }
            }
        }
    }

    private fun createIntegrated3dsToken(vaultToken: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result =
                createIntegratedThreeDSTokenUseCase(
                    accessToken = accessToken.value,
                    request = CreateIntegratedThreeDSTokenRequest(
                        customer = Customer(
                            paymentSource = PaymentSource(
                                gatewayId = BuildConfig.GATEWAY_ID,
                                vaultToken = vaultToken
                            )
                        )
                    )
                )
            result.onSuccess { threeDSResult ->
                when (threeDSResult.status) {
                    ThreeDSToken.ThreeDSStatus.NOT_SUPPORTED -> captureCardCharge(vaultToken)
                    ThreeDSToken.ThreeDSStatus.PRE_AUTH_PENDING -> {
                        _stateFlow.update { state ->
                            state.copy(
                                threeDSToken = threeDSResult,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    else -> _stateFlow.update { state ->
                        state.copy(
                            isLoading = false,
                            error = THREE_DS_STATUS_ERROR
                        )
                    }
                }
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        isLoading = false,
                        error = it.message ?: THREE_DS_CARD_ERROR
                    )
                }
            }
        }
    }

    private fun initiateWalletTransaction(
        manualCapture: Boolean = false,
        request: InitiateWalletRequest,
        callback: (String) -> Unit
    ) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result =
                initiateWalletTransactionUseCase(manualCapture = manualCapture, request = request)
            result.onSuccess { charge ->
                charge.walletToken?.let { callback(it) }
                _stateFlow.update { state ->
                    state.copy(isLoading = false, error = null, walletChargeResult = charge)
                }
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        isLoading = false,
                        error = it.message ?: CHARGE_TRANSACTION_ERROR
                    )
                }
            }
        }
    }

    fun handleCardResult(result: Result<CardResult>) {
        result.onSuccess {
            createSessionVaultToken(it.token)
        }.onFailure {
            _stateFlow.update { state ->
                state.copy(error = TOKENISE_CARD_ERROR)
            }
        }
    }

    fun handleClickToPayResult(result: Result<String>) {
        result.onSuccess {
            handleTokenResult(it)
            // If successful, we directly start charge flow
            createSessionVaultToken(cardToken = it)
        }.onFailure {
            _stateFlow.update { state ->
                state.copy(error = TOKENISE_CARD_ERROR)
            }
        }
        result.onSuccess {
            // If successful, we directly start charge flow
            createSessionVaultToken(cardToken = it)
        }
    }

    fun handleThreeDSResult(vaultToken: String, result: Result<ThreeDSResult>) {
        result.onSuccess {
            _stateFlow.update { state -> state.copy(threeDSToken = null) }
            captureCardCharge(vaultToken)
        }.onFailure {
            _stateFlow.update { state ->
                state.copy(isLoading = false, error = CHARGE_TRANSACTION_ERROR)
            }
        }
    }

    fun handleChargeResult(result: Result<ChargeResponse>) {
        result.onSuccess {
            _stateFlow.update { state ->
                state.copy(
                    isLoading = false,
                    chargeResult = it,
                    threeDSToken = null,
                    vaultToken = null,
                    cardToken = null
                )
            }
        }.onFailure {
            _stateFlow.update { state ->
                state.copy(isLoading = false, error = CHARGE_TRANSACTION_ERROR)
            }
        }
    }

    fun handleFlyPayResult(result: Result<String>) {
        val chargeId = _stateFlow.value.walletChargeResult?.chargeId
        if (chargeId != null) {
            result.onSuccess {
                captureWalletCharge(chargeId)
                _stateFlow.update { state ->
                    state.copy(
                        flyPayResult = it
                    )
                }
            }.onFailure {
                _stateFlow.update { state ->
                    state.copy(isLoading = false, error = CHARGE_TRANSACTION_ERROR)
                }
            }
        } else {
            _stateFlow.update { state ->
                state.copy(isLoading = false, error = CHARGE_TRANSACTION_ERROR)
            }
        }
    }

    fun handleAfterpayResult(result: Result<String>) {
        val chargeId = _stateFlow.value.walletChargeResult?.chargeId
        if (chargeId != null) {
            result.onSuccess {
                captureWalletCharge(chargeId)
                _stateFlow.update { state ->
                    state.copy(
                        afterPayResult = it
                    )
                }
            }.onFailure {
                _stateFlow.update { state ->
                    state.copy(isLoading = false, error = CHARGE_TRANSACTION_ERROR)
                }
            }
        } else {
            _stateFlow.update { state ->
                state.copy(isLoading = false, error = CHARGE_TRANSACTION_ERROR)
            }
        }
    }

    private fun handleTokenResult(token: String) {
        _stateFlow.update { state ->
            state.copy(cardToken = token)
        }
    }

    private fun captureWalletCharge(chargeId: String) {
        // This is an optional capture charge logic if "?capture=false"
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result = captureWalletChargeUseCase(chargeId)
            result.onSuccess { data ->
                _stateFlow.update { state ->
                    state.copy(
                        isLoading = false,
                        walletChargeResult = state.walletChargeResult?.copy(status = data.status),
                    )
                }
            }.onFailure {
                _stateFlow.update { state ->
                    state.copy(isLoading = false, error = CHARGE_TRANSACTION_ERROR)
                }
            }
        }
    }

    private fun captureCardCharge(vaultToken: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true, threeDSToken = null)
            }
            val request = CaptureCardChargeRequest(
                currency = AU_CURRENCY_CODE, customer = Customer(
                    paymentSource = PaymentSource(
                        gatewayId = BuildConfig.GATEWAY_ID,
                        vaultToken = vaultToken
                    )
                )
            )
            handleChargeResult(captureCardChargeTokenUseCase(request))
        }
    }

}

data class CheckoutUIState(
    val isLoading: Boolean = false,
    val cardToken: String? = null,
    val vaultToken: String? = null,
    val walletChargeResult: WalletCharge? = null,
    val threeDSToken: ThreeDSToken? = null,
    val chargeResult: ChargeResponse? = null,
    val flyPayResult: String? = null,
    val afterPayResult: String? = null,
    val error: String? = null
)