package com.paydock.sample.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.sample.feature.checkout.data.api.dto.CreateIntentRequest
import com.paydock.sample.feature.checkout.domain.usecase.CreateDirectCheckoutIntentUseCase
import com.paydock.sample.feature.checkout.domain.usecase.CreateTemplateCheckoutIntentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutWorkflowViewModel @Inject constructor(
    private val createTemplateCheckoutIntentUseCase: CreateTemplateCheckoutIntentUseCase,
    private val createDirectCheckoutIntentUseCase: CreateDirectCheckoutIntentUseCase
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<CheckoutWorkflowUIState> =
        MutableStateFlow(CheckoutWorkflowUIState())
    val stateFlow: StateFlow<CheckoutWorkflowUIState> = _stateFlow

    fun resetResultState() {
        _stateFlow.update { state ->
            state.copy(
                intentToken = null,
                error = null
            )
        }
    }

    fun createTemplateIntentToken() {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val request = CreateIntentRequest.TemplateIntentRequest()
            val result = createTemplateCheckoutIntentUseCase(request)
            result.onSuccess { token ->
                _stateFlow.update { state ->
                    state.copy(isLoading = false, intentToken = token)
                }
            }.onFailure {
                _stateFlow.update { state ->
                    state.copy(isLoading = false, error = it.message)
                }
            }
        }
    }

    fun createDirectIntentToken() {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val request = CreateIntentRequest.DirectIntentRequest()
            val result = createDirectCheckoutIntentUseCase(request)
            result.onSuccess { token ->
                _stateFlow.update { state ->
                    state.copy(isLoading = false, intentToken = token)
                }
            }.onFailure {
                _stateFlow.update { state ->
                    state.copy(isLoading = false, error = it.message)
                }
            }
        }
    }
}

data class CheckoutWorkflowUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val intentToken: String? = null
)