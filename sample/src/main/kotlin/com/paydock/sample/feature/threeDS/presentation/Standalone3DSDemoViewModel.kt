package com.paydock.sample.feature.threeDS.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.sample.core.THREE_DS_CARD_ERROR
import com.paydock.sample.core.TOKENISE_CARD_ERROR
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.card.domain.usecase.CreateCardVaultTokenUseCase
import com.paydock.sample.feature.checkout.data.api.dto.ChargesCustomerDTO
import com.paydock.sample.feature.checkout.models.Standalone3DSFlowState
import com.paydock.sample.feature.checkout.models.Standalone3DSPhase
import com.paydock.sample.feature.config.data.GlobalConfigRepository
import com.paydock.sample.feature.config.data.WidgetConfigRepository
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.usecase.CreateStandaloneThreeDSTokenUseCase
import com.paydock.sample.feature.threeDS.presentation.state.Standalone3DSDemoOutcome
import com.paydock.sample.feature.threeDS.presentation.state.Standalone3DSDemoUIState
import com.paydock.sample.feature.threeDS.presentation.state.Standalone3DSFlowType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the Widgets > Standalone 3DS demo: creates a vault token and a 3DS token, then maps the
 * widget's progress and result callbacks onto the playground phases.
 */
@HiltViewModel
class Standalone3DSDemoViewModel @Inject constructor(
    private val createCardVaultTokenUseCase: CreateCardVaultTokenUseCase,
    private val createStandaloneThreeDSTokenUseCase: CreateStandaloneThreeDSTokenUseCase,
    private val globalConfigRepository: GlobalConfigRepository,
    private val widgetConfigRepository: WidgetConfigRepository,
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(Standalone3DSDemoUIState())
    val stateFlow: StateFlow<Standalone3DSDemoUIState> = _stateFlow.asStateFlow()

    private var setupJob: Job? = null
    private var flowType = Standalone3DSFlowType.FRICTIONLESS

    init {
        start()
    }

    /**
     * Starts (or restarts) the demo with fresh tokens.
     */
    fun start() {
        setupJob?.cancel()
        flowType = Standalone3DSFlowType.FRICTIONLESS
        _stateFlow.update { Standalone3DSDemoUIState(attempt = it.attempt + 1) }
        setupJob = viewModelScope.launch {
            val accessToken = globalConfigRepository.globalConfig.value.apiAccessToken
            val vaultToken = createCardVaultTokenUseCase(
                accessToken,
                VaultTokenRequest.CreateCardVaultTokenRequest()
            ).getOrElse {
                fail(declined = false, errorMessage = it.message ?: TOKENISE_CARD_ERROR)
                return@launch
            }
            val request = CreateStandaloneThreeDSTokenRequest(
                amount = widgetConfigRepository.widgetConfig.value.cartAmount,
                currency = globalConfigRepository.globalConfig.value.currencyCode,
                customer = ChargesCustomerDTO(
                    paymentSource = ChargesCustomerDTO.PaymentSourceDTO(vaultToken = vaultToken)
                )
            )
            val threeDSToken = createStandaloneThreeDSTokenUseCase(accessToken, request)
                .getOrElse {
                    fail(declined = false, errorMessage = it.message ?: THREE_DS_CARD_ERROR)
                    return@launch
                }.token
            if (threeDSToken.isNullOrBlank()) {
                fail(declined = false, errorMessage = THREE_DS_CARD_ERROR)
                return@launch
            }
            _stateFlow.update {
                it.copy(
                    flow = Standalone3DSFlowState(Standalone3DSPhase.VERIFYING),
                    threeDSToken = threeDSToken
                )
            }
        }
    }

    /**
     * Maps the widget's progress events onto the playground phases.
     */
    fun handleProgress(progress: Standalone3DSProgress) {
        val current = _stateFlow.value.flow
        // Ignore late progress once the flow reached a terminal phase
        if (current.phase.isTerminal()) return
        val next = when (progress) {
            is Standalone3DSProgress.ChallengeStarted -> {
                flowType = Standalone3DSFlowType.CHALLENGE
                Standalone3DSFlowState(Standalone3DSPhase.CHALLENGE_LOADING, challengeShown = true)
            }

            is Standalone3DSProgress.ChallengeLoaded -> current.copy(phase = Standalone3DSPhase.CHALLENGE)
            is Standalone3DSProgress.ChallengeCompleted -> current.copy(phase = Standalone3DSPhase.FINALIZING)
            is Standalone3DSProgress.Decoupled -> {
                flowType = Standalone3DSFlowType.DECOUPLED
                Standalone3DSFlowState(
                    phase = Standalone3DSPhase.DECOUPLED,
                    decoupledDescription = progress.description
                )
            }
        }
        _stateFlow.update { it.copy(flow = next) }
    }

    /**
     * Handles the widget's completion callback. Only success, reject and error end the flow: the
     * challenge / decoupled / info results duplicate progress events and are ignored here.
     */
    fun handleResult(result: Result<Standalone3DSResult>) {
        if (_stateFlow.value.flow.phase.isTerminal()) return
        result.onSuccess {
            when (it.event) {
                StandaloneEventType.CHARGE_AUTH_SUCCESS -> finish(Standalone3DSPhase.SUCCESS, it)
                StandaloneEventType.CHARGE_AUTH_REJECT -> finish(Standalone3DSPhase.FAILED, it, declined = true)
                StandaloneEventType.CHARGE_ERROR -> finish(Standalone3DSPhase.FAILED, it)
                StandaloneEventType.CHARGE_AUTH_CHALLENGE,
                StandaloneEventType.CHARGE_AUTH_DECOUPLED,
                StandaloneEventType.CHARGE_AUTH_INFO -> Unit
            }
        }.onFailure {
            fail(declined = false, errorMessage = it.toError().displayableMessage)
        }
    }

    private fun finish(phase: Standalone3DSPhase, result: Standalone3DSResult, declined: Boolean = false) {
        // Dropping the token removes the widget, closing the bank's iframe
        _stateFlow.update {
            it.copy(
                flow = Standalone3DSFlowState(phase, declined = declined),
                threeDSToken = null,
                outcome = outcome(
                    charge3dsId = result.charge3dsId,
                    status = result.status,
                    description = result.resultDescription
                )
            )
        }
    }

    private fun fail(declined: Boolean, errorMessage: String?) {
        _stateFlow.update {
            it.copy(
                flow = Standalone3DSFlowState(Standalone3DSPhase.FAILED, declined = declined),
                threeDSToken = null,
                outcome = outcome(errorMessage = errorMessage)
            )
        }
    }

    private fun outcome(
        charge3dsId: String? = null,
        status: String? = null,
        description: String? = null,
        errorMessage: String? = null
    ) = Standalone3DSDemoOutcome(
        charge3dsId = charge3dsId,
        status = status,
        description = description,
        flowType = flowType,
        amount = widgetConfigRepository.widgetConfig.value.cartAmount,
        currency = globalConfigRepository.globalConfig.value.currencyCode,
        errorMessage = errorMessage
    )

    private fun Standalone3DSPhase.isTerminal() =
        this == Standalone3DSPhase.SUCCESS || this == Standalone3DSPhase.FAILED
}
