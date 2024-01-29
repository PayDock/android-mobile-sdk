/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.sample.feature.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.sample.core.TOKENISE_CARD_ERROR
import com.paydock.sample.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.card.domain.usecase.CreateCardVaultTokenUseCase
import com.paydock.sample.feature.card.domain.usecase.TokeniseCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val tokeniseCardUseCase: TokeniseCardUseCase,
    private val createCardVaultTokenUseCase: CreateCardVaultTokenUseCase
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<CardUIState> =
        MutableStateFlow(CardUIState())
    val stateFlow: StateFlow<CardUIState> = _stateFlow

    fun tokeniseCardDetails() {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result = tokeniseCardUseCase(TokeniseCardRequest())
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
    val error: String? = null
)