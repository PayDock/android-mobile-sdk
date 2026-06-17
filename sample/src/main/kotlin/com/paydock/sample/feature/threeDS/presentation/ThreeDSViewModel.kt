package com.paydock.sample.feature.threeDS.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.sample.core.THREE_DS_CARD_ERROR
import com.paydock.sample.core.THREE_DS_NOT_SUPPORTED_ERROR
import com.paydock.sample.feature.checkout.data.api.dto.ChargesCustomerDTO
import com.paydock.sample.feature.config.data.GlobalConfigRepository
import com.paydock.sample.feature.config.data.WidgetConfigRepository
import com.paydock.sample.feature.threeDS.data.api.dto.CreateMPGS3dsTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.model.ThreeDSToken
import com.paydock.sample.feature.threeDS.domain.usecase.CreateMPGS3dsTokenUseCase
import com.paydock.sample.feature.threeDS.domain.usecase.CreateStandaloneThreeDSTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreeDSViewModel @Inject constructor(
    private val createMPGS3dsTokenUseCase: CreateMPGS3dsTokenUseCase,
    private val createStandaloneThreeDSTokenUseCase: CreateStandaloneThreeDSTokenUseCase,
    private val globalConfigRepository: GlobalConfigRepository,
    private val widgetConfigRepository: WidgetConfigRepository,
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<ThreeDSUIState> =
        MutableStateFlow(ThreeDSUIState())
    val stateFlow: StateFlow<ThreeDSUIState> = _stateFlow

    fun createMPGS3dsToken(cardToken: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val amount = widgetConfigRepository.widgetConfig.value.cartAmount
            val currency = globalConfigRepository.globalConfig.value.currencyCode
            val accessToken = globalConfigRepository.globalConfig.value.apiAccessToken
            val request = CreateMPGS3dsTokenRequest(
                token = cardToken,
                amount = amount,
                currency = currency
            )
            val result = createMPGS3dsTokenUseCase(accessToken, request)
            result.onSuccess { threeDSResult ->
                when (threeDSResult.status) {
                    ThreeDSToken.ThreeDSStatus.PRE_AUTH_PENDING -> {
                        _stateFlow.update { state ->
                            state.copy(token = threeDSResult.token, isLoading = false, error = null)
                        }
                    }

                    ThreeDSToken.ThreeDSStatus.NOT_SUPPORTED -> {
                        _stateFlow.update { state ->
                            state.copy(
                                token = null,
                                isLoading = false,
                                error = THREE_DS_NOT_SUPPORTED_ERROR
                            )
                        }
                    }

                    else -> {
                        _stateFlow.update { state ->
                            state.copy(
                                token = null,
                                isLoading = false,
                                error = THREE_DS_CARD_ERROR
                            )
                        }
                    }
                }
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        token = null,
                        isLoading = false,
                        error = it.message ?: THREE_DS_CARD_ERROR
                    )
                }
            }
        }
    }

    fun createStandalone3dsToken(vaultToken: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val amount = widgetConfigRepository.widgetConfig.value.cartAmount
            val currency = globalConfigRepository.globalConfig.value.currencyCode
            val accessToken = globalConfigRepository.globalConfig.value.apiAccessToken
            val request = CreateStandaloneThreeDSTokenRequest(
                amount = amount,
                currency = currency,
                customer = ChargesCustomerDTO(
                    paymentSource = ChargesCustomerDTO.PaymentSourceDTO(vaultToken = vaultToken)
                )
            )
            val result = createStandaloneThreeDSTokenUseCase(accessToken, request)
            result.onSuccess { threeDSResult ->
                _stateFlow.update { state ->
                    state.copy(token = threeDSResult.token, isLoading = false, error = null)
                }
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        token = null,
                        isLoading = false,
                        error = it.message ?: THREE_DS_CARD_ERROR
                    )
                }
            }
        }
    }

    fun resetResultState() {
        _stateFlow.update { state ->
            state.copy(token = null, error = null)
        }
    }

    fun setIsLoading(isLoading: Boolean) {
        _stateFlow.update { state ->
            state.copy(isLoading = isLoading)
        }
    }
}

data class ThreeDSUIState(
    val isLoading: Boolean = true,
    val token: String? = null,
    val error: String? = null,
)