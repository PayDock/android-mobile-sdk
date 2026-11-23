package com.paydock.feature.threeDS.standalone.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.Standalone3DSException
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeCompletedSource
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeLoadedReason
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.Standalone3DSAuthenticationFlow
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeError
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeErrorEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeResult
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.StandaloneChargeEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
import com.paydock.feature.threeDS.standalone.presentation.state.Standalone3DSUIState
import com.paydock.telemetry.TelemetryEvent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The authentication flow [Standalone3DSViewModel] sets on Standalone 3DS results, per charge ID (MD-56, R_02 to
 * R_06), and proof that the states and payment telemetry are otherwise unchanged.
 */
@RunWith(MockitoJUnitRunner::class)
internal class Standalone3DSViewModelAuthenticationFlowTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: Standalone3DSViewModel
    private lateinit var telemetry: RecordingTelemetry

    private val chargeId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID

    private val challengeEvent = Standalone3DSEvent.ChargeAuthChallengeEvent(
        data = StandaloneChargeEventData(charge3dsId = chargeId, status = StandaloneStatus.PENDING)
    )
    private val challengeLoadedEvent = Standalone3DSEvent.ChargeAuthChallengeLoadedEvent(
        data = StandaloneChargeEventData(charge3dsId = chargeId, status = null, reason = "load")
    )
    private val challengeCompletedEvent = Standalone3DSEvent.ChargeAuthChallengeCompletedEvent(
        data = StandaloneChargeEventData(charge3dsId = chargeId, status = null, source = "poll")
    )
    private val decoupledEvent = Standalone3DSEvent.ChargeAuthDecoupledEvent(
        data = StandaloneChargeEventData(
            charge3dsId = chargeId,
            status = StandaloneStatus.PENDING,
            result = ChargeResult(description = "Approve in your banking app")
        )
    )
    private val infoEvent = Standalone3DSEvent.ChargeAuthInfoEvent(
        data = StandaloneChargeEventData(charge3dsId = chargeId, status = StandaloneStatus.PENDING)
    )
    private val successEvent = Standalone3DSEvent.ChargeAuthSuccessEvent(
        data = StandaloneChargeEventData(charge3dsId = chargeId, status = StandaloneStatus.SUCCESS)
    )
    private val rejectEvent = Standalone3DSEvent.ChargeAuthRejectEvent(
        data = StandaloneChargeEventData(charge3dsId = chargeId, status = StandaloneStatus.ERROR)
    )
    private val errorEvent = Standalone3DSEvent.ChargeErrorEvent(
        data = ChargeErrorEventData(error = ChargeError("error"), charge3dsId = null)
    )

    @Before
    fun setup() {
        telemetry = RecordingTelemetry()
        viewModel = Standalone3DSViewModel(dispatchersProvider, telemetry)
    }

    private fun successFor(id: String?) = Standalone3DSEvent.ChargeAuthSuccessEvent(
        data = StandaloneChargeEventData(charge3dsId = id, status = StandaloneStatus.SUCCESS)
    )

    private fun challengeFor(id: String?) = Standalone3DSEvent.ChargeAuthChallengeEvent(
        data = StandaloneChargeEventData(charge3dsId = id, status = StandaloneStatus.PENDING)
    )

    /** The results the view model hands to the widget's completion, in order. */
    private suspend fun resultsOf(vararg events: Standalone3DSEvent): List<Standalone3DSResult> =
        collectStates(*events).filterIsInstance<Standalone3DSUIState.Success>().map { it.result }

    private suspend fun finalFlowOf(vararg events: Standalone3DSEvent): Standalone3DSAuthenticationFlow? =
        resultsOf(*events).last().authenticationFlow

    /** Sets every result's flow back to null: the states as they were before the flow existed. */
    private fun List<Standalone3DSUIState>.withoutFlow() = map { state ->
        if (state is Standalone3DSUIState.Success) {
            Standalone3DSUIState.Success(state.result.copy(authenticationFlow = null))
        } else {
            state
        }
    }

    @Test
    fun `success with nothing before is frictionless`() = runTest {
        assertEquals(Standalone3DSAuthenticationFlow.FRICTIONLESS, finalFlowOf(successEvent))
    }

    @Test
    fun `reject with nothing before is frictionless`() = runTest {
        assertEquals(Standalone3DSAuthenticationFlow.FRICTIONLESS, finalFlowOf(rejectEvent))
    }

    @Test
    fun `challenge then success is challenge`() = runTest {
        assertEquals(
            Standalone3DSAuthenticationFlow.CHALLENGE,
            finalFlowOf(challengeEvent, challengeLoadedEvent, challengeCompletedEvent, successEvent)
        )
    }

    @Test
    fun `challenge then reject is challenge`() = runTest {
        assertEquals(
            Standalone3DSAuthenticationFlow.CHALLENGE,
            finalFlowOf(challengeEvent, challengeLoadedEvent, challengeCompletedEvent, rejectEvent)
        )
    }

    @Test
    fun `decoupled then success is decoupled`() = runTest {
        assertEquals(
            Standalone3DSAuthenticationFlow.DECOUPLED,
            finalFlowOf(decoupledEvent, challengeCompletedEvent, successEvent)
        )
    }

    @Test
    fun `decoupled then reject is decoupled`() = runTest {
        assertEquals(
            Standalone3DSAuthenticationFlow.DECOUPLED,
            finalFlowOf(decoupledEvent, challengeCompletedEvent, rejectEvent)
        )
    }

    @Test
    fun `challenge, decoupled, info and charge error results carry null, and a mapping failure emits no result`() =
        runTest {
            val results = resultsOf(challengeEvent, decoupledEvent, infoEvent, errorEvent)

            assertEquals(
                listOf(
                    StandaloneEventType.CHARGE_AUTH_CHALLENGE,
                    StandaloneEventType.CHARGE_AUTH_DECOUPLED,
                    StandaloneEventType.CHARGE_AUTH_INFO,
                    StandaloneEventType.CHARGE_ERROR
                ),
                results.map { it.event }
            )
            results.forEach { assertNull(it.authenticationFlow) }

            viewModel.eventFlow.test {
                viewModel.handleEventResult(
                    Result.failure(Standalone3DSException.EventMappingException("synthetic"))
                )

                assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
                assertIs<Standalone3DSUIState.Error>(awaitItem())
                expectNoEvents()
            }
        }

    @Test
    fun `flows are tracked per charge ID and end with the final result`() = runTest {
        val results = resultsOf(
            challengeFor(CHARGE_A),
            successFor(CHARGE_B),
            successFor(CHARGE_A),
            successFor(CHARGE_A)
        )

        assertEquals(
            listOf(
                CHARGE_A to null,
                CHARGE_B to Standalone3DSAuthenticationFlow.FRICTIONLESS,
                CHARGE_A to Standalone3DSAuthenticationFlow.CHALLENGE,
                CHARGE_A to Standalone3DSAuthenticationFlow.FRICTIONLESS
            ),
            results.map { it.charge3dsId to it.authenticationFlow }
        )
    }

    @Test
    fun `the latest of challenge and decoupled wins, and a charge error ends the tracking`() = runTest {
        assertEquals(
            Standalone3DSAuthenticationFlow.DECOUPLED,
            finalFlowOf(challengeEvent, decoupledEvent, successEvent)
        )
        val afterError = Standalone3DSEvent.ChargeErrorEvent(
            data = ChargeErrorEventData(error = ChargeError("error"), charge3dsId = chargeId)
        )
        assertEquals(Standalone3DSAuthenticationFlow.FRICTIONLESS, finalFlowOf(challengeEvent, afterError, successEvent))
    }

    @Test
    fun `a null charge ID is tracked under its own key`() = runTest {
        assertEquals(
            listOf(null, Standalone3DSAuthenticationFlow.CHALLENGE),
            resultsOf(challengeFor(null), successFor(null)).map { it.authenticationFlow }
        )
    }

    @Test
    fun `the emitted states without the flow are unchanged for the frictionless, challenge, decoupled and error flows`() =
        runTest {
            fun success(type: StandaloneEventType, id: String? = chargeId, description: String? = null) =
                Standalone3DSUIState.Success(Standalone3DSResult(type, id, resultDescription = description))

            val loaded = Standalone3DSProgress.ChallengeLoaded(chargeId, ChallengeLoadedReason.LOAD)
            val completed = Standalone3DSProgress.ChallengeCompleted(chargeId, ChallengeCompletedSource.POLL)
            val description = "Approve in your banking app"

            assertEquals(
                listOf(
                    success(StandaloneEventType.CHARGE_AUTH_INFO),
                    Standalone3DSUIState.Loading(false),
                    success(StandaloneEventType.CHARGE_AUTH_SUCCESS)
                ),
                collectStates(infoEvent, successEvent).withoutFlow()
            )
            assertEquals(
                listOf(
                    Standalone3DSUIState.Loading(true),
                    Standalone3DSUIState.Progress(Standalone3DSProgress.ChallengeStarted(chargeId)),
                    success(StandaloneEventType.CHARGE_AUTH_CHALLENGE),
                    Standalone3DSUIState.Loading(false),
                    Standalone3DSUIState.Progress(loaded),
                    Standalone3DSUIState.Loading(true),
                    Standalone3DSUIState.Progress(completed),
                    Standalone3DSUIState.Loading(false),
                    success(StandaloneEventType.CHARGE_AUTH_SUCCESS)
                ),
                collectStates(challengeEvent, challengeLoadedEvent, challengeCompletedEvent, successEvent).withoutFlow()
            )
            assertEquals(
                listOf(
                    Standalone3DSUIState.Loading(false),
                    Standalone3DSUIState.Progress(Standalone3DSProgress.Decoupled(chargeId, description)),
                    success(StandaloneEventType.CHARGE_AUTH_DECOUPLED, description = description),
                    Standalone3DSUIState.Loading(true),
                    Standalone3DSUIState.Progress(completed),
                    Standalone3DSUIState.Loading(false),
                    success(StandaloneEventType.CHARGE_AUTH_SUCCESS)
                ),
                collectStates(decoupledEvent, challengeCompletedEvent, successEvent).withoutFlow()
            )
            assertEquals(
                listOf(
                    Standalone3DSUIState.Loading(true),
                    Standalone3DSUIState.Progress(Standalone3DSProgress.ChallengeStarted(chargeId)),
                    success(StandaloneEventType.CHARGE_AUTH_CHALLENGE),
                    Standalone3DSUIState.Loading(false),
                    success(StandaloneEventType.CHARGE_ERROR, id = null)
                ),
                collectStates(challengeEvent, errorEvent).withoutFlow()
            )
        }

    @Test
    fun `the payment telemetry is unchanged for the frictionless, challenge, decoupled and error flows`() {
        val expected = listOf(
            listOf(infoEvent, successEvent) to (TelemetryEvent.PaymentOutcome.SUCCESS to null),
            listOf(challengeEvent, challengeLoadedEvent, challengeCompletedEvent, successEvent) to
                (TelemetryEvent.PaymentOutcome.SUCCESS to null),
            listOf(decoupledEvent, challengeCompletedEvent, rejectEvent) to
                (TelemetryEvent.PaymentOutcome.DECLINED to null),
            listOf(challengeEvent, errorEvent) to (TelemetryEvent.PaymentOutcome.ERROR to "CHARGE_ERROR"),
        )
        expected.forEach { (events, outcome) ->
            val reporting = RecordingTelemetry()

            val finished = finishOf(*events.toTypedArray(), reporting = reporting).single()

            assertEquals(1, reporting.paymentsStarted.size)
            assertEquals(reporting.paymentsStarted.single().paymentId, finished.paymentId)
            assertEquals(outcome, finished.outcome to finished.errorCode)
            assertTrue(reporting.sdkErrors.isEmpty())
        }
    }

    /**
     * Feeds the events through the view model and collects every emitted state, in order.
     */
    private suspend fun collectStates(vararg events: Standalone3DSEvent): List<Standalone3DSUIState> {
        val states = mutableListOf<Standalone3DSUIState>()
        viewModel.eventFlow.test {
            events.forEach { viewModel.handleEventResult(Result.success(it)) }
            states += cancelAndConsumeRemainingEvents()
                .filterIsInstance<app.cash.turbine.Event.Item<Standalone3DSUIState>>()
                .map { it.value }
        }
        return states
    }

    /** Runs the events through a new view model with a started attempt and returns its finished payments. */
    private fun finishOf(vararg events: Standalone3DSEvent, reporting: RecordingTelemetry) =
        Standalone3DSViewModel(dispatchersProvider, reporting).run {
            startAttempt(TOKEN)
            events.forEach { handleEventResult(Result.success(it)) }
            reporting.paymentsFinished
        }

    private companion object {
        const val TOKEN = "3ds-token-1"
        const val CHARGE_A = "charge-a"
        const val CHARGE_B = "charge-b"
    }
}
