package com.paydock.sample.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.sample.feature.customer.data.api.dto.CreateCustomerOTTRequest
import com.paydock.sample.feature.customer.domain.model.Customer
import com.paydock.sample.feature.customer.domain.usecase.CreateCustomerOTTUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val createCustomerOTTUseCase: CreateCustomerOTTUseCase,
) : ViewModel(), WidgetLoadingDelegate {

    private val _stateFlow: MutableStateFlow<AccountUIState> = MutableStateFlow(AccountUIState())
    val stateFlow: StateFlow<AccountUIState> = _stateFlow

    fun resetResultState() {
        _stateFlow.update { state ->
            state.copy(
                customer = null,
                error = null
            )
        }
    }

    fun createCustomer(token: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true, customer = null)
            }
            val request = CreateCustomerOTTRequest(token)
            createCustomerOTTUseCase(request).onSuccess { result ->
                _stateFlow.update { state ->
                    state.copy(isLoading = false, customer = result)
                }
            }.onFailure { error ->
                _stateFlow.update { state ->
                    state.copy(isLoading = false, error = error.toError().displayableMessage)
                }
            }
        }
    }

    override fun widgetLoadingDidStart() {
        _stateFlow.update { state ->
            state.copy(isLoading = true)
        }
    }

    override fun widgetLoadingDidFinish() {
        _stateFlow.update { state ->
            state.copy(isLoading = false)
        }
    }
}

data class AccountUIState(
    val isLoading: Boolean = false,
    val customer: Customer? = null,
    val error: String? = null,
)