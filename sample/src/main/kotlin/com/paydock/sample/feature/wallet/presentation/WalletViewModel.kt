package com.paydock.sample.feature.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.api.charges.domain.model.WalletType
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.feature.charges.data.api.dto.ChargesCustomerDTO
import com.paydock.sample.feature.charges.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.charges.domain.model.WalletCharge
import com.paydock.sample.feature.charges.domain.usecase.InitiateWalletTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(private val initiateWalletTransactionUseCase: InitiateWalletTransactionUseCase) :
    ViewModel() {

    private val _stateFlow: MutableStateFlow<WalletTransactionUIState> =
        MutableStateFlow(WalletTransactionUIState())
    val stateFlow: StateFlow<WalletTransactionUIState> = _stateFlow

    private fun resetResultState() {
        _stateFlow.update { state ->
            state.copy(walletChargeResult = null, error = null)
        }
    }

    private fun initiateWalletTransaction(
        manualCapture: Boolean = false,
        request: InitiateWalletRequest,
        callback: (String) -> Unit,
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
                        walletChargeResult = null,
                        isLoading = false,
                        error = it.message ?: CHARGE_TRANSACTION_ERROR
                    )
                }
            }
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
            currency = AU_CURRENCY_CODE,
            customer = ChargesCustomerDTO(
                email = "annaanna@yopmail.com",
                paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                    gatewayId = BuildConfig.GATEWAY_ID_PAY_PAL,
                    walletType = WalletType.PAY_PAL.type
                )
            )
        )
    }

    private fun createFlyPayWalletRequest(): InitiateWalletRequest {
        return InitiateWalletRequest(
            currency = AU_CURRENCY_CODE,
            customer = ChargesCustomerDTO(
                paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                    gatewayId = BuildConfig.GATEWAY_ID_FLY_PAY,
                    walletType = WalletType.FLY_PAY.type
                )
            )
        )
    }

    private fun createGoogleWalletRequest(): InitiateWalletRequest {
        return InitiateWalletRequest(
            currency = AU_CURRENCY_CODE,
            customer = ChargesCustomerDTO(
                paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
                    gatewayId = BuildConfig.GATEWAY_ID_GOOGLE_PAY,
                    walletType = WalletType.GOOGLE.type
                )
            )
        )

    }

    private fun createAfterpayWalletRequest(): InitiateWalletRequest {
        return InitiateWalletRequest(
            currency = AU_CURRENCY_CODE,
            customer = ChargesCustomerDTO(
                email = "david.cameron@paydock.com",
                firstName = "David",
                lastName = "Cameron",
                paymentSource = ChargesCustomerDTO.PaymentSourceDTO(
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
            meta = InitiateWalletRequest.MetaDTO(
                storeId = "1234",
                storeName = MERCHANT_NAME,
                successUrl = "https://paydock-integration.netlify.app/success",
                errorUrl = "https://paydock-integration.netlify.app/error"
            ),
            shippingDTO = InitiateWalletRequest.ShippingDTO(),
            itemDTOS = listOf(InitiateWalletRequest.ItemDTO())
        )
    }
}

data class WalletTransactionUIState(
    val isLoading: Boolean = false,
    val walletChargeResult: WalletCharge? = null,
    val error: String? = null,
)