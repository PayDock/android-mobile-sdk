package com.paydock.sample.feature.threeDS.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.sample.core.THREE_DS_CARD_ERROR
import com.paydock.sample.core.presentation.utils.AccessTokenProvider
import com.paydock.sample.feature.threeDS.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.usecase.CreateIntegratedThreeDSTokenUseCase
import com.paydock.sample.feature.threeDS.domain.usecase.CreateStandaloneThreeDSTokenUseCase
import com.paydock.sample.feature.wallet.data.api.dto.Customer
import com.paydock.sample.feature.wallet.data.api.dto.PaymentSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreeDSViewModel @Inject constructor(
    private val accessTokenProvider: AccessTokenProvider,
    private val createIntegratedThreeDSTokenUseCase: CreateIntegratedThreeDSTokenUseCase,
    private val createStandaloneThreeDSTokenUseCase: CreateStandaloneThreeDSTokenUseCase,
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<ThreeDSUIState> =
        MutableStateFlow(ThreeDSUIState())
    val stateFlow: StateFlow<ThreeDSUIState> = _stateFlow

    fun createIntegrated3dsToken(cardToken: String) {
        viewModelScope.launch {
            val accessToken = accessTokenProvider.accessToken.value
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result =
                createIntegratedThreeDSTokenUseCase(
                    accessToken,
                    CreateIntegratedThreeDSTokenRequest(token = cardToken)
                )
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

    fun createStandalone3dsToken(vaultToken: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result =
                createStandaloneThreeDSTokenUseCase(
                    CreateStandaloneThreeDSTokenRequest(
                        customer = Customer(
                            paymentSource = PaymentSource(vaultToken = vaultToken)
                        )
                    )
                )
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
}

data class ThreeDSUIState(
    val isLoading: Boolean = true,
    val token: String? = null,
    val error: String? = null,
)