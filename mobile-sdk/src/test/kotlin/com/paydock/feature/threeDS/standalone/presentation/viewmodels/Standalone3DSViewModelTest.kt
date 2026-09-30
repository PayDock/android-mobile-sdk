package com.paydock.feature.threeDS.standalone.presentation.viewmodels

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.Standalone3DSException
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeCompletedSource
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeLoadedReason
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeError
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeErrorEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeResult
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.StandaloneChargeEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
import com.paydock.feature.threeDS.standalone.presentation.state.Standalone3DSUIState
import com.paydock.feature.threeDS.standalone.presentation.utils.Standalone3DSLoadingController
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
internal class Standalone3DSViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: Standalone3DSViewModel

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
        viewModel = Standalone3DSViewModel(dispatchersProvider)
    }

    private suspend fun ReceiveTurbine<Standalone3DSUIState>.awaitSuccess(
        eventType: StandaloneEventType,
        charge3dsId: String? = chargeId,
        description: String? = null
    ) {
        awaitItem().let { state ->
            assertIs<Standalone3DSUIState.Success>(state)
            assertEquals(Standalone3DSResult(eventType, charge3dsId, resultDescription = description), state.result)
        }
    }

    @Test
    fun `handleEventResult with chargeAuthSuccess event should finish loading then update event state`() = runTest {
        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.success(successEvent))

            assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
            awaitSuccess(StandaloneEventType.CHARGE_AUTH_SUCCESS)
            expectNoEvents()
        }
    }

    @Test
    fun `handleEventResult with chargeAuthReject event should finish loading then update event state`() = runTest {
        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.success(rejectEvent))

            assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
            awaitSuccess(StandaloneEventType.CHARGE_AUTH_REJECT)
            expectNoEvents()
        }
    }

    @Test
    fun `handleEventResult with error event should finish loading then update event state`() = runTest {
        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.success(errorEvent))

            assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
            awaitSuccess(StandaloneEventType.CHARGE_ERROR, charge3dsId = null)
            expectNoEvents()
        }
    }

    @Test
    fun `handleEventResult with chargeAuthChallenge event should keep loading, emit progress and update event state`() =
        runTest {
            viewModel.eventFlow.test {
                viewModel.handleEventResult(Result.success(challengeEvent))

                assertEquals(Standalone3DSUIState.Loading(true), awaitItem())
                assertEquals(
                    Standalone3DSUIState.Progress(Standalone3DSProgress.ChallengeStarted(chargeId)),
                    awaitItem()
                )
                awaitSuccess(StandaloneEventType.CHARGE_AUTH_CHALLENGE)
                expectNoEvents()
            }
        }

    @Test
    fun `handleEventResult with chargeAuthChallengeLoaded event should finish loading and only emit progress`() = runTest {
        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.success(challengeLoadedEvent))

            assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
            assertEquals(
                Standalone3DSUIState.Progress(
                    Standalone3DSProgress.ChallengeLoaded(chargeId, ChallengeLoadedReason.LOAD)
                ),
                awaitItem()
            )
            // Never reaches completion
            expectNoEvents()
        }
    }

    @Test
    fun `handleEventResult with chargeAuthChallengeCompleted event should start loading and only emit progress`() =
        runTest {
            viewModel.eventFlow.test {
                viewModel.handleEventResult(Result.success(challengeCompletedEvent))

                assertEquals(Standalone3DSUIState.Loading(true), awaitItem())
                assertEquals(
                    Standalone3DSUIState.Progress(
                        Standalone3DSProgress.ChallengeCompleted(chargeId, ChallengeCompletedSource.POLL)
                    ),
                    awaitItem()
                )
                // Never reaches completion
                expectNoEvents()
            }
        }

    @Test
    fun `handleEventResult with chargeAuthDecoupled event should finish loading, emit progress and update event state`() =
        runTest {
            viewModel.eventFlow.test {
                viewModel.handleEventResult(Result.success(decoupledEvent))

                assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
                assertEquals(
                    Standalone3DSUIState.Progress(
                        Standalone3DSProgress.Decoupled(chargeId, "Approve in your banking app")
                    ),
                    awaitItem()
                )
                awaitSuccess(StandaloneEventType.CHARGE_AUTH_DECOUPLED, description = "Approve in your banking app")
                expectNoEvents()
            }
        }

    @Test
    fun `handleEventResult with chargeAuthInfo event should not change loading and update event state`() = runTest {
        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.success(infoEvent))

            awaitSuccess(StandaloneEventType.CHARGE_AUTH_INFO)
            expectNoEvents()
        }
    }

    @Test
    fun `handleEventResult with failure should finish loading then update error state`() = runTest {
        viewModel.eventFlow.test {
            val message = "3DS Error has occurred!"
            viewModel.handleEventResult(
                Result.failure(Standalone3DSException.EventMappingException(message))
            )

            assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
            awaitItem().let { state ->
                assertIs<Standalone3DSUIState.Error>(state)
                assertIs<Standalone3DSException.EventMappingException>(state.exception)
                assertEquals(message, state.exception.message)
            }
            expectNoEvents()
        }
    }

    @Test
    fun `frictionless flow starts and finishes loading once`() = runTest {
        assertEquals(
            listOf(START, FINISH),
            runFlowThroughLoadingController(infoEvent, successEvent)
        )
    }

    @Test
    fun `challenge flow finishes loading when the challenge is visible and starts again while confirming`() = runTest {
        assertEquals(
            listOf(START, FINISH, START, FINISH),
            runFlowThroughLoadingController(challengeEvent, challengeLoadedEvent, challengeCompletedEvent, rejectEvent)
        )
    }

    @Test
    fun `decoupled flow finishes loading while waiting for approval and starts again while confirming`() = runTest {
        assertEquals(
            listOf(START, FINISH, START, FINISH),
            runFlowThroughLoadingController(decoupledEvent, challengeCompletedEvent, successEvent)
        )
    }

    @Test
    fun `challenge flow ending in error keeps delegate calls balanced`() = runTest {
        assertEquals(
            listOf(START, FINISH),
            runFlowThroughLoadingController(challengeEvent, errorEvent)
        )
    }

    @Test
    fun `progress-only events never reach completion`() = runTest {
        val states = collectStates(challengeEvent, challengeLoadedEvent, challengeCompletedEvent, successEvent)
        val completionEvents = states.filterIsInstance<Standalone3DSUIState.Success>().map { it.result.event }

        assertEquals(
            listOf(StandaloneEventType.CHARGE_AUTH_CHALLENGE, StandaloneEventType.CHARGE_AUTH_SUCCESS),
            completionEvents
        )
        assertEquals(
            listOf(
                Standalone3DSProgress.ChallengeStarted(chargeId),
                Standalone3DSProgress.ChallengeLoaded(chargeId, ChallengeLoadedReason.LOAD),
                Standalone3DSProgress.ChallengeCompleted(chargeId, ChallengeCompletedSource.POLL)
            ),
            states.filterIsInstance<Standalone3DSUIState.Progress>().map { it.progress }
        )
        // The completion for the final event is delivered after the loader was finished
        val lastLoading = states.indexOfLast { it is Standalone3DSUIState.Loading }
        assertTrue(lastLoading < states.indexOfLast { it is Standalone3DSUIState.Success })
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

    /**
     * Simulates the widget: starts loading at launch and applies every emitted loading change to a
     * [Standalone3DSLoadingController], recording the resulting delegate calls.
     */
    private suspend fun runFlowThroughLoadingController(vararg events: Standalone3DSEvent): List<String> {
        val calls = mutableListOf<String>()
        val delegate = object : WidgetLoadingDelegate {
            override fun widgetLoadingDidStart() {
                calls += START
            }

            override fun widgetLoadingDidFinish() {
                calls += FINISH
            }
        }
        val controller = Standalone3DSLoadingController()
        controller.start(delegate)
        collectStates(*events)
            .filterIsInstance<Standalone3DSUIState.Loading>()
            .forEach { controller.update(it.isLoading, delegate) }
        return calls
    }

    private companion object {
        const val START = "start"
        const val FINISH = "finish"
    }
}
