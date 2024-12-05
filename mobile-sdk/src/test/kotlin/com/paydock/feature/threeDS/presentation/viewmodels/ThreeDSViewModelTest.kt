package com.paydock.feature.threeDS.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.domain.model.integration.enums.EventType
import com.paydock.feature.threeDS.domain.model.ui.ChargeError
import com.paydock.feature.threeDS.domain.model.ui.ChargeErrorEventData
import com.paydock.feature.threeDS.domain.model.ui.ChargeEventData
import com.paydock.feature.threeDS.domain.model.ui.ChargeResult
import com.paydock.feature.threeDS.domain.model.ui.ThreeDSEvent
import com.paydock.feature.threeDS.presentation.state.ThreeDSUIState
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
internal class ThreeDSViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: ThreeDSViewModel

    @Before
    fun setup() {
        viewModel = ThreeDSViewModel(dispatchersProvider)
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthSuccess event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeAuthSuccessEvent(
            data = ChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = "authenticated"
            )
        )
        // CHECK
        viewModel.stateFlow.test {
            // ACTION
            viewModel.updateThreeDSEvent(event)
            // Initial state
            assertIs<ThreeDSUIState.Idle>(awaitItem())
            awaitItem().let { state ->
                assertIs<ThreeDSUIState.Success>(state)
                assertEquals(EventType.CHARGE_AUTH_SUCCESS, state.result.event)
                assertEquals(MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID, state.result.charge3dsId)
            }
        }
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthReject event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeAuthRejectEvent(
            data = ChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = "not_authenticated"
            )
        )
        // CHECK
        viewModel.stateFlow.test {
            // ACTION
            viewModel.updateThreeDSEvent(event)
            // Initial state
            assertIs<ThreeDSUIState.Idle>(awaitItem())
            awaitItem().let { state ->
                assertIs<ThreeDSUIState.Success>(state)
                assertEquals(EventType.CHARGE_AUTH_REJECT, state.result.event)
                assertEquals(MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID, state.result.charge3dsId)
            }
        }
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthChallenge event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeAuthChallengeEvent(
            data = ChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = "pending"
            )
        )
        viewModel.stateFlow.test {
            // ACTION
            viewModel.updateThreeDSEvent(event)
            // Initial state
            assertIs<ThreeDSUIState.Idle>(awaitItem())
            awaitItem().let { state ->
                assertIs<ThreeDSUIState.Success>(state)
                assertEquals(EventType.CHARGE_AUTH_CHALLENGE, state.result.event)
                assertEquals(MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID, state.result.charge3dsId)
            }
        }
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthDecoupled event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeAuthDecoupledEvent(
            data = ChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = "auth",
                info = "authentication info example"
            )
        )
        viewModel.stateFlow.test {
            // ACTION
            viewModel.updateThreeDSEvent(event)
            // Initial state
            assertIs<ThreeDSUIState.Idle>(awaitItem())
            awaitItem().let { state ->
                assertIs<ThreeDSUIState.Success>(state)
                assertEquals(EventType.CHARGE_AUTH_DECOUPLED, state.result.event)
                assertEquals(MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID, state.result.charge3dsId)
            }
        }
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthInfo event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeAuthInfoEvent(
            data = ChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = "decoupled",
                result = ChargeResult(
                    description = "test description example"
                )
            )
        )
        viewModel.stateFlow.test {
            // ACTION
            viewModel.updateThreeDSEvent(event)
            // Initial state
            assertIs<ThreeDSUIState.Idle>(awaitItem())
            awaitItem().let { state ->
                assertIs<ThreeDSUIState.Success>(state)
                assertEquals(EventType.CHARGE_AUTH_INFO, state.result.event)
                assertEquals(MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID, state.result.charge3dsId)
            }
        }
    }

    @Test
    fun `updateThreeDSEvent with error event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeErrorEvent(
            data = ChargeErrorEventData(
                error = ChargeError(message = "3DS Error has occurred!"),
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
            )
        )
        // ACTION
        viewModel.stateFlow.test {
            // ACTION
            viewModel.updateThreeDSEvent(event)
            // Initial state
            assertIs<ThreeDSUIState.Idle>(awaitItem())
            awaitItem().let { state ->
                assertIs<ThreeDSUIState.Success>(state)
                assertEquals(EventType.CHARGE_ERROR, state.result.event)
                assertEquals(MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID, state.result.charge3dsId)
            }
        }
    }
}
