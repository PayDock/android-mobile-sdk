package com.paydock.feature.threeDS.integrated.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.Integrated3DSException
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.integrated.domain.model.integration.enums.IntegratedEventType
import com.paydock.feature.threeDS.integrated.domain.model.ui.Integrated3DSEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.IntegratedChargeEventData
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedStatus
import com.paydock.feature.threeDS.integrated.presentation.state.Integrated3DSUIState
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
internal class Integrated3DSViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: Integrated3DSViewModel

    @Before
    fun setup() {
        viewModel = Integrated3DSViewModel(dispatchersProvider)
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthSuccess event should update event state`() =
        runTest {
            val event = Integrated3DSEvent.ChargeAuthSuccessEvent(
                data = IntegratedChargeEventData(
                    event = IntegratedEvent.CHARGE_AUTH_SUCCESS,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = IntegratedStatus.AUTHENTICATED
                )
            )
            // CHECK
            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<Integrated3DSUIState.Success>(state)
                    assertEquals(IntegratedEventType.CHARGE_AUTH_SUCCESS, state.result.event)
                    assertEquals(
                        MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                        state.result.charge3dsId
                    )
                }
            }
        }

    @Test
    fun `updateThreeDSEvent with chargeAuthReject event should update event state`() =
        runTest {
            val event = Integrated3DSEvent.ChargeAuthRejectEvent(
                data = IntegratedChargeEventData(
                    event = IntegratedEvent.CHARGE_AUTH_REJECT,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = IntegratedStatus.AUTHENTICATED
                )
            )
            // CHECK
            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<Integrated3DSUIState.Success>(state)
                    assertEquals(IntegratedEventType.CHARGE_AUTH_REJECT, state.result.event)
                    assertEquals(
                        MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                        state.result.charge3dsId
                    )
                }
            }
        }

    @Test
    fun `updateThreeDSEvent with chargeAuth event should update event state`() =
        runTest {
            val event = Integrated3DSEvent.ChargeAuthEvent(
                data = IntegratedChargeEventData(
                    event = IntegratedEvent.CHARGE_AUTH,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = IntegratedStatus.NOT_AUTHENTICATED
                )
            )
            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<Integrated3DSUIState.Success>(state)
                    assertEquals(IntegratedEventType.CHARGE_AUTH, state.result.event)
                    assertEquals(
                        MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                        state.result.charge3dsId
                    )
                }
            }
        }

    @Test
    fun `updateThreeDSEvent with additionalDataCollectSuccess event should update event state`() =
        runTest {
            val event = Integrated3DSEvent.AdditionalDataCollectSuccessEvent(
                data = IntegratedChargeEventData(
                    event = IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = IntegratedStatus.NOT_AUTHENTICATED
                )
            )

            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<Integrated3DSUIState.Success>(state)
                    assertEquals(
                        IntegratedEventType.ADDITIONAL_DATA_COLLECT_SUCCESS,
                        state.result.event
                    )
                    assertEquals(
                        MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                        state.result.charge3dsId
                    )
                }
            }
        }

    @Test
    fun `updateThreeDSEvent with additionalDataCollectReject event should update event state`() =
        runTest {
            val event = Integrated3DSEvent.AdditionalDataCollectRejectEvent(
                data = IntegratedChargeEventData(
                    event = IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = IntegratedStatus.NOT_AUTHENTICATED
                )
            )
            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<Integrated3DSUIState.Success>(state)
                    assertEquals(
                        IntegratedEventType.ADDITIONAL_DATA_COLLECT_REJECT,
                        state.result.event
                    )
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
            viewModel.handleEventResult(Result.failure(Integrated3DSException.EventMappingException(message)))
            // Initial state
            awaitItem().let { state ->
                assertIs<Integrated3DSUIState.Error>(state)
                assertIs<Integrated3DSException.EventMappingException>(state.exception)
                assertEquals(message, state.exception.message)
            }
        }
    }
}
