package com.paydock.feature.threeDS.standalone.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.Standalone3DSException
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeResult
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.StandaloneChargeEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
import com.paydock.feature.threeDS.standalone.presentation.state.Standalone3DSUIState
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(MockitoJUnitRunner::class)
internal class Standalone3DSViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: Standalone3DSViewModel

    @Before
    fun setup() {
        viewModel = Standalone3DSViewModel(dispatchersProvider)
    }

    @Test
    fun `handleEventResult with chargeAuthSuccess event should update event state`() = runTest {
        val event = Standalone3DSEvent.ChargeAuthSuccessEvent(
            data = StandaloneChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = StandaloneStatus.SUCCESS
            )
        )
        // CHECK
        viewModel.eventFlow.test {
            // ACTION
            viewModel.handleEventResult(Result.success(event))
            // Initial state
            awaitItem().let { state ->
                assertIs<Standalone3DSUIState.Success>(state)
                assertEquals(StandaloneEventType.CHARGE_AUTH_SUCCESS, state.result.event)
                assertEquals(
                    MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    state.result.charge3dsId
                )
            }
        }
    }

    @Test
    fun `handleEventResult with chargeAuthReject event should update event state`() = runTest {
        val event = Standalone3DSEvent.ChargeAuthRejectEvent(
            data = StandaloneChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = StandaloneStatus.ERROR
            )
        )
        // CHECK
        viewModel.eventFlow.test {
            // ACTION
            viewModel.handleEventResult(Result.success(event))
            // Initial state
            awaitItem().let { state ->
                assertIs<Standalone3DSUIState.Success>(state)
                assertEquals(StandaloneEventType.CHARGE_AUTH_REJECT, state.result.event)
                assertEquals(
                    MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    state.result.charge3dsId
                )
            }
        }
    }

    @Test
    fun `handleEventResult with chargeAuthChallenge event should update event state`() = runTest {
        val event = Standalone3DSEvent.ChargeAuthChallengeEvent(
            data = StandaloneChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = StandaloneStatus.PENDING
            )
        )
        viewModel.eventFlow.test {
            // ACTION
            viewModel.handleEventResult(Result.success(event))
            // Initial state
            awaitItem().let { state ->
                assertIs<Standalone3DSUIState.Success>(state)
                assertEquals(StandaloneEventType.CHARGE_AUTH_CHALLENGE, state.result.event)
                assertEquals(
                    MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    state.result.charge3dsId
                )
            }
        }
    }

    @Test
    fun `handleEventResult with chargeAuthDecoupled event should update event state`() = runTest {
        val event = Standalone3DSEvent.ChargeAuthDecoupledEvent(
            data = StandaloneChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = StandaloneStatus.PENDING
            )
        )

        viewModel.eventFlow.test {
            // ACTION
            viewModel.handleEventResult(Result.success(event))
            // Initial state
            awaitItem().let { state ->
                assertIs<Standalone3DSUIState.Success>(state)
                assertEquals(StandaloneEventType.CHARGE_AUTH_DECOUPLED, state.result.event)
                assertEquals(
                    MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    state.result.charge3dsId
                )
            }
        }
    }

    @Test
    fun `handleEventResult with chargeAuthInfo event should update event state`() = runTest {
        val event = Standalone3DSEvent.ChargeAuthInfoEvent(
            data = StandaloneChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = StandaloneStatus.PENDING,
                result = ChargeResult(
                    description = "test description example"
                )
            )
        )
        viewModel.eventFlow.test {
            // ACTION
            viewModel.handleEventResult(Result.success(event))
            // Initial state
            awaitItem().let { state ->
                assertIs<Standalone3DSUIState.Success>(state)
                assertEquals(StandaloneEventType.CHARGE_AUTH_INFO, state.result.event)
                assertEquals(
                    MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    state.result.charge3dsId
                )
            }
        }
    }

    @Test
    fun `handleEventResult with error event should update event state`() = runTest {
        // ACTION
        viewModel.eventFlow.test {
            // ACTION
            val message = "3DS Error has occurred!"
            viewModel.handleEventResult(
                Result.failure(
                    Standalone3DSException.EventMappingException(
                        message
                    )
                )
            )
            // Initial state
            awaitItem().let { state ->
                assertIs<Standalone3DSUIState.Error>(state)
                assertIs<Standalone3DSException.EventMappingException>(state.exception)
                assertEquals(message, state.exception.message)
            }
        }
    }
}
