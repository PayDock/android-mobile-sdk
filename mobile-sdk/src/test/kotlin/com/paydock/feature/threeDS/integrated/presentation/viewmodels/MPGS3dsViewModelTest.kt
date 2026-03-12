package com.paydock.feature.threeDS.integrated.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.MPGS3dsException
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.integrated.domain.model.integration.enums.MPGS3dsEventType
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsChargeEventData
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.MPGS3dsStatus
import com.paydock.feature.threeDS.integrated.presentation.state.MPGS3dsUIState
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
internal class MPGS3dsViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: MPGS3dsViewModel

    @Before
    fun setup() {
        viewModel = MPGS3dsViewModel(dispatchersProvider)
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthSuccess event should update event state`() =
        runTest {
            val event = MPGS3dsEvent.ChargeAuthSuccessEvent(
                data = MPGS3dsChargeEventData(
                    event = IntegratedEvent.CHARGE_AUTH_SUCCESS,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = MPGS3dsStatus.AUTHENTICATED
                )
            )
            // CHECK
            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<MPGS3dsUIState.Success>(state)
                    assertEquals(MPGS3dsEventType.CHARGE_AUTH_SUCCESS, state.result.event)
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
            val event = MPGS3dsEvent.ChargeAuthRejectEvent(
                data = MPGS3dsChargeEventData(
                    event = IntegratedEvent.CHARGE_AUTH_REJECT,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = MPGS3dsStatus.AUTHENTICATED
                )
            )
            // CHECK
            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<MPGS3dsUIState.Success>(state)
                    assertEquals(MPGS3dsEventType.CHARGE_AUTH_REJECT, state.result.event)
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
            val event = MPGS3dsEvent.ChargeAuthEvent(
                data = MPGS3dsChargeEventData(
                    event = IntegratedEvent.CHARGE_AUTH,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = MPGS3dsStatus.NOT_AUTHENTICATED
                )
            )
            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<MPGS3dsUIState.Success>(state)
                    assertEquals(MPGS3dsEventType.CHARGE_AUTH, state.result.event)
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
            val event = MPGS3dsEvent.AdditionalDataCollectSuccessEvent(
                data = MPGS3dsChargeEventData(
                    event = IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = MPGS3dsStatus.NOT_AUTHENTICATED
                )
            )

            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<MPGS3dsUIState.Success>(state)
                    assertEquals(
                        MPGS3dsEventType.ADDITIONAL_DATA_COLLECT_SUCCESS,
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
            val event = MPGS3dsEvent.AdditionalDataCollectRejectEvent(
                data = MPGS3dsChargeEventData(
                    event = IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    status = MPGS3dsStatus.NOT_AUTHENTICATED
                )
            )
            viewModel.eventFlow.test {
                // ACTION
                viewModel.handleEventResult(Result.success(event))
                // Initial state
                awaitItem().let { state ->
                    assertIs<MPGS3dsUIState.Success>(state)
                    assertEquals(
                        MPGS3dsEventType.ADDITIONAL_DATA_COLLECT_REJECT,
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
            viewModel.handleEventResult(Result.failure(MPGS3dsException.EventMappingException(message)))
            // Initial state
            awaitItem().let { state ->
                assertIs<MPGS3dsUIState.Error>(state)
                assertIs<MPGS3dsException.EventMappingException>(state.exception)
                assertEquals(message, state.exception.message)
            }
        }
    }
}
