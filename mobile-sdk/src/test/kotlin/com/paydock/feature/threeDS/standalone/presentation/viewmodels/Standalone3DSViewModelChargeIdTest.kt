package com.paydock.feature.threeDS.standalone.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.Standalone3DSException
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.Standalone3DSAuthenticationFlow
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeError
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeErrorEventData
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The 3DS charge ID and flow [Standalone3DSViewModel] puts on its payment records (MD-58, D25, R_01, R_02, R_06): the
 * token's charge ID, else the first event's, on the start and the finish; the merchant result's authentication flow
 * on a success or reject finish, and no flow on an error finish.
 */
@RunWith(MockitoJUnitRunner::class)
internal class Standalone3DSViewModelChargeIdTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: Standalone3DSViewModel
    private lateinit var telemetry: RecordingTelemetry

    @Before
    fun setup() {
        telemetry = RecordingTelemetry()
        viewModel = Standalone3DSViewModel(dispatchersProvider, telemetry)
    }

    private fun challenge(id: String? = EVENT_ID) = Standalone3DSEvent.ChargeAuthChallengeEvent(
        data = StandaloneChargeEventData(charge3dsId = id, status = StandaloneStatus.PENDING)
    )

    private fun decoupled(id: String? = EVENT_ID) = Standalone3DSEvent.ChargeAuthDecoupledEvent(
        data = StandaloneChargeEventData(charge3dsId = id, status = StandaloneStatus.PENDING)
    )

    private fun success(id: String? = EVENT_ID) = Standalone3DSEvent.ChargeAuthSuccessEvent(
        data = StandaloneChargeEventData(charge3dsId = id, status = StandaloneStatus.SUCCESS)
    )

    private fun reject(id: String? = EVENT_ID) = Standalone3DSEvent.ChargeAuthRejectEvent(
        data = StandaloneChargeEventData(charge3dsId = id, status = StandaloneStatus.ERROR)
    )

    private fun chargeError(id: String? = EVENT_ID) = Standalone3DSEvent.ChargeErrorEvent(
        data = ChargeErrorEventData(error = ChargeError("error"), charge3dsId = id)
    )

    private fun feed(vararg events: Standalone3DSEvent) {
        events.forEach { viewModel.handleEventResult(Result.success(it)) }
    }

    private val started get() = telemetry.paymentsStarted.single()
    private val finished get() = telemetry.paymentsFinished.single()

    // region charge ID

    @Test
    fun `the token's charge ID is on the start and the finish`() {
        viewModel.startAttempt(TOKEN_ID)
        feed(challenge(TOKEN_ID), success(TOKEN_ID))

        assertEquals(TOKEN_ID, started.charge3dsId)
        assertEquals(TOKEN_ID, finished.charge3dsId)
    }

    @Test
    fun `without a token ID the start has none and the finish has the first event's`() {
        viewModel.startAttempt(null)
        feed(challenge(EVENT_ID), success(EVENT_ID))

        assertNull(started.charge3dsId)
        assertEquals(EVENT_ID, finished.charge3dsId)
    }

    @Test
    fun `an empty token ID counts as none`() {
        viewModel.startAttempt("")
        feed(success(EVENT_ID))

        assertNull(started.charge3dsId)
        assertEquals(EVENT_ID, finished.charge3dsId)
    }

    @Test
    fun `a later event's different ID does not replace the first one`() {
        viewModel.startAttempt()
        feed(challenge(null), challenge(EVENT_ID), success(OTHER_ID))

        assertEquals(EVENT_ID, finished.charge3dsId)
    }

    @Test
    fun `the token's ID wins over an event's`() {
        viewModel.startAttempt(TOKEN_ID)
        feed(challenge(EVENT_ID), reject(EVENT_ID))

        assertEquals(TOKEN_ID, started.charge3dsId)
        assertEquals(TOKEN_ID, finished.charge3dsId)
    }

    @Test
    fun `with neither a token nor an event ID there is no charge ID on either`() {
        viewModel.startAttempt()
        feed(challenge(null), success(null))

        assertNull(started.charge3dsId)
        assertNull(finished.charge3dsId)
    }

    // endregion

    // region flow

    @Test
    fun `challenge then success is challenge, equal to the merchant result's flow`() = runTest {
        assertFlow(Standalone3DSAuthenticationFlow.CHALLENGE, TelemetryEvent.ThreeDSFlow.CHALLENGE, challenge(), success())
    }

    @Test
    fun `success alone is frictionless, equal to the merchant result's flow`() = runTest {
        assertFlow(Standalone3DSAuthenticationFlow.FRICTIONLESS, TelemetryEvent.ThreeDSFlow.FRICTIONLESS, success())
    }

    @Test
    fun `decoupled then reject is decoupled, equal to the merchant result's flow`() = runTest {
        assertFlow(Standalone3DSAuthenticationFlow.DECOUPLED, TelemetryEvent.ThreeDSFlow.DECOUPLED, decoupled(), reject())
    }

    @Test
    fun `challenge then reject is challenge, equal to the merchant result's flow`() = runTest {
        assertFlow(Standalone3DSAuthenticationFlow.CHALLENGE, TelemetryEvent.ThreeDSFlow.CHALLENGE, challenge(), reject())
    }

    // endregion

    // region error finishes

    @Test
    fun `a charge error finish carries the charge ID and no flow`() {
        viewModel.startAttempt(TOKEN_ID)
        feed(challenge(), chargeError())

        assertEquals(TelemetryEvent.PaymentOutcome.ERROR, finished.outcome)
        assertEquals("CHARGE_ERROR", finished.errorCode)
        assertEquals(TOKEN_ID, finished.charge3dsId)
        assertNull(finished.flow)
    }

    @Test
    fun `a charge error's own ID is kept when nothing came before`() {
        viewModel.startAttempt()
        feed(chargeError(EVENT_ID))

        assertEquals(EVENT_ID, finished.charge3dsId)
        assertNull(finished.flow)
    }

    @Test
    fun `a mapping failure finish carries the charge ID and no flow`() {
        viewModel.startAttempt()
        feed(challenge())
        viewModel.handleEventResult(Result.failure(Standalone3DSException.EventMappingException("synthetic")))

        assertEquals(TelemetryEvent.PaymentOutcome.ERROR, finished.outcome)
        assertEquals("EventMappingException", finished.errorCode)
        assertEquals(EVENT_ID, finished.charge3dsId)
        assertNull(finished.flow)
    }

    @Test
    fun `a widget failure finish carries the charge ID and no flow`() {
        viewModel.startAttempt(TOKEN_ID)
        viewModel.failAttempt(Standalone3DSException.WebViewException(code = -1, displayableMessage = "web view"))

        assertEquals(TelemetryEvent.PaymentOutcome.ERROR, finished.outcome)
        assertEquals(TOKEN_ID, finished.charge3dsId)
        assertNull(finished.flow)
    }

    @Test
    fun `with telemetry inactive nothing is reported`() {
        val inactive = RecordingTelemetry(isActive = false)
        Standalone3DSViewModel(dispatchersProvider, inactive).run {
            startAttempt(TOKEN_ID)
            handleEventResult(Result.success(challenge()))
            handleEventResult(Result.success(success()))
        }

        assertTrue(inactive.events.isEmpty())
    }

    // endregion

    /**
     * Starts an attempt, feeds [events] and asserts the merchant's final result has [merchantFlow] and the finish has
     * [telemetryFlow], the same flow.
     */
    private suspend fun assertFlow(
        merchantFlow: Standalone3DSAuthenticationFlow,
        telemetryFlow: TelemetryEvent.ThreeDSFlow,
        vararg events: Standalone3DSEvent,
    ) {
        viewModel.startAttempt()
        val results = mutableListOf<Standalone3DSResult>()
        viewModel.eventFlow.test {
            feed(*events)
            results += cancelAndConsumeRemainingEvents()
                .filterIsInstance<app.cash.turbine.Event.Item<Standalone3DSUIState>>()
                .map { it.value }
                .filterIsInstance<Standalone3DSUIState.Success>()
                .map { it.result }
        }

        assertEquals(merchantFlow, results.last().authenticationFlow)
        assertEquals(telemetryFlow, finished.flow)
        assertEquals(merchantFlow.name, finished.flow?.name)
        assertEquals(EVENT_ID, finished.charge3dsId)
    }

    private companion object {
        const val TOKEN_ID = "6741b9c2e4a17f0d3c5b8a9e"
        const val EVENT_ID = "5f2a7d0e6b348e41c2d73b9f"
        const val OTHER_ID = "0a9d5e72f348e7a3c9154d2b"
    }
}
