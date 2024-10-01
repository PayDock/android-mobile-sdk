package com.paydock.feature.threeDS.presentation.viewmodels

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.domain.model.EventType
import com.paydock.feature.threeDS.presentation.model.ChargeError
import com.paydock.feature.threeDS.presentation.model.ChargeErrorEventData
import com.paydock.feature.threeDS.presentation.model.ChargeEventData
import com.paydock.feature.threeDS.presentation.model.ChargeResult
import com.paydock.feature.threeDS.presentation.model.ThreeDSEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner::class)
class ThreeDSViewModelTest : BaseKoinUnitTest() {

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
                charge3dsId = "ba7d839a-a868-4bb0-9763-015451fea9fb",
                status = "authenticated"
            )
        )
        // ACTION
        viewModel.updateThreeDSEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("ba7d839a-a868-4bb0-9763-015451fea9fb", state.result?.charge3dsId)
        assertEquals(EventType.CHARGE_AUTH_SUCCESS, state.result?.event)
        assertEquals("authenticated", state.status)
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthReject event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeAuthRejectEvent(
            data = ChargeEventData(
                charge3dsId = "ba7d839a-a868-4bb0-9763-015451fea9fb",
                status = "not_authenticated"
            )
        )
        // ACTION
        viewModel.updateThreeDSEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals(
            MobileSDKConstants.Errors.THREE_DS_REJECTED_ERROR,
            state.error.message
        )
        assertEquals("ba7d839a-a868-4bb0-9763-015451fea9fb", state.result?.charge3dsId)
        assertEquals(EventType.CHARGE_AUTH_REJECT, state.result?.event)
        assertEquals("not_authenticated", state.status)
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthChallenge event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeAuthChallengeEvent(
            data = ChargeEventData(
                charge3dsId = "ba7d839a-a868-4bb0-9763-015451fea9fb",
                status = "pending"
            )
        )
        // ACTION
        viewModel.updateThreeDSEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("ba7d839a-a868-4bb0-9763-015451fea9fb", state.result?.charge3dsId)
        assertEquals(EventType.CHARGE_AUTH_CHALLENGE, state.result?.event)
        assertEquals("pending", state.status)
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthDecoupled event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeAuthDecoupledEvent(
            data = ChargeEventData(
                charge3dsId = "ba7d839a-a868-4bb0-9763-015451fea9fb",
                status = "auth",
                info = "authentication info example"
            )
        )
        // ACTION
        viewModel.updateThreeDSEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("ba7d839a-a868-4bb0-9763-015451fea9fb", state.result?.charge3dsId)
        assertEquals(EventType.CHARGE_AUTH_DECOUPLED, state.result?.event)
        assertEquals("auth", state.status)
    }

    @Test
    fun `updateThreeDSEvent with chargeAuthInfo event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeAuthInfoEvent(
            data = ChargeEventData(
                charge3dsId = "ba7d839a-a868-4bb0-9763-015451fea9fb",
                status = "decoupled",
                result = ChargeResult(
                    description = "test description example"
                )
            )
        )
        // ACTION
        viewModel.updateThreeDSEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("ba7d839a-a868-4bb0-9763-015451fea9fb", state.result?.charge3dsId)
        assertEquals(EventType.CHARGE_AUTH_INFO, state.result?.event)
        assertEquals("decoupled", state.status)
    }

    @Test
    fun `updateThreeDSEvent with error event should update event state`() = runTest {
        val event = ThreeDSEvent.ChargeErrorEvent(
            data = ChargeErrorEventData(
                error = ChargeError(message = "3DS Error has occurred!"),
                charge3dsId = "ba7d839a-a868-4bb0-9763-015451fea9fb"
            )
        )
        // ACTION
        viewModel.updateThreeDSEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isLoading)
        assertEquals("ba7d839a-a868-4bb0-9763-015451fea9fb", state.result?.charge3dsId)
        assertEquals(EventType.CHARGE_ERROR, state.result?.event)
        assertNotNull(state.error)
        assertEquals(
            event.data.error.message,
            state.error.message
        )
    }
}
