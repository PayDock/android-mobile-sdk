package com.paydock.sample.feature.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.sample.core.TOKENISE_CARD_ERROR
import com.paydock.sample.core.presentation.utils.AccessTokenProvider
import com.paydock.sample.feature.tokens.data.api.dto.TokeniseCardRequest
import com.paydock.sample.feature.tokens.domain.usecase.TokeniseCardUseCase
import com.paydock.sample.feature.vaults.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.vaults.domain.usecase.CreateCardVaultTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val accessTokenProvider: AccessTokenProvider,
    private val tokeniseCardUseCase: TokeniseCardUseCase,
    private val createCardVaultTokenUseCase: CreateCardVaultTokenUseCase,
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<CardUIState> =
        MutableStateFlow(CardUIState())
    val stateFlow: StateFlow<CardUIState> = _stateFlow

    fun tokeniseCardDetails() {
        viewModelScope.launch {
            val accessToken = accessTokenProvider.accessToken.value
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result = tokeniseCardUseCase(accessToken, TokeniseCardRequest())
            result.onSuccess { token ->
                _stateFlow.update { state ->
                    state.copy(token = token, isLoading = false, error = null)
                }
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        token = null,
                        isLoading = false,
                        error = it.message ?: TOKENISE_CARD_ERROR
                    )
                }
            }
        }
    }

    fun createCardVaultTokenDetails() {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result =
                createCardVaultTokenUseCase(VaultTokenRequest.CreateCardVaultTokenRequest())
            result.onSuccess { token ->
                _stateFlow.update { state ->
                    state.copy(token = token, isLoading = false, error = null)
                }
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        token = null,
                        isLoading = false,
                        error = it.message ?: TOKENISE_CARD_ERROR
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

data class CardUIState(
    val isLoading: Boolean = true,
    val token: String? = null,
    val error: String? = null,
)