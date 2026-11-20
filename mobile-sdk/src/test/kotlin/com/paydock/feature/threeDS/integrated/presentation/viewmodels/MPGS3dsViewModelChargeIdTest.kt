package com.paydock.feature.threeDS.integrated.presentation.viewmodels

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.MPGS3dsException
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsChargeEventData
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.MPGS3dsStatus
import com.paydock.telemetry.TelemetryEvent
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
 * The 3DS charge ID [MPGS3dsViewModel] puts on its payment records (MD-58, D25, R_01, R_02, R_06): the token's charge
 * ID, else the first event's, on the start and the finish; no MPGS 3DS finish carries a flow.
 */
@RunWith(MockitoJUnitRunner::class)
internal class MPGS3dsViewModelChargeIdTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: MPGS3dsViewModel
    private lateinit var telemetry: RecordingTelemetry

    @Before
    fun setup() {
        telemetry = RecordingTelemetry()
        viewModel = MPGS3dsViewModel(dispatchersProvider, telemetry)
    }

    private fun data(type: IntegratedEvent, id: String?) =
        MPGS3dsChargeEventData(status = MPGS3dsStatus.AUTHENTICATED, charge3dsId = id, event = type)

    private fun chargeAuth(id: String? = EVENT_ID) =
        MPGS3dsEvent.ChargeAuthEvent(data = data(IntegratedEvent.CHARGE_AUTH, id))

    private fun success(id: String? = EVENT_ID) =
        MPGS3dsEvent.ChargeAuthSuccessEvent(data = data(IntegratedEvent.CHARGE_AUTH_SUCCESS, id))

    private fun reject(id: String? = EVENT_ID) =
        MPGS3dsEvent.ChargeAuthRejectEvent(data = data(IntegratedEvent.CHARGE_AUTH_REJECT, id))

    private fun additionalDataReject(id: String? = EVENT_ID) =
        MPGS3dsEvent.AdditionalDataCollectRejectEvent(data = data(IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT, id))

    private fun cancelled(id: String? = EVENT_ID) =
        MPGS3dsEvent.ChargeAuthCancelledEvent(data = data(IntegratedEvent.CHARGE_AUTH_CANCELLED, id))

    private fun feed(vararg events: MPGS3dsEvent) {
        events.forEach { viewModel.handleEventResult(Result.success(it)) }
    }

    private val started get() = telemetry.paymentsStarted.single()
    private val finished get() = telemetry.paymentsFinished.single()

    // region charge ID

    @Test
    fun `the token's charge ID is on the start and the finish`() {
        viewModel.startAttempt(TOKEN_ID)
        feed(chargeAuth(TOKEN_ID), success(TOKEN_ID))

        assertEquals(TOKEN_ID, started.charge3dsId)
        assertEquals(TOKEN_ID, finished.charge3dsId)
    }

    @Test
    fun `without a token ID the start has none and the finish has the first event's`() {
        viewModel.startAttempt(null)
        feed(chargeAuth(EVENT_ID), success(EVENT_ID))

        assertNull(started.charge3dsId)
        assertEquals(EVENT_ID, finished.charge3dsId)
    }

    @Test
    fun `a later event's different ID does not replace the first one`() {
        viewModel.startAttempt("")
        feed(chargeAuth(null), chargeAuth(EVENT_ID), success(OTHER_ID))

        assertEquals(EVENT_ID, finished.charge3dsId)
    }

    @Test
    fun `the token's ID wins over an event's`() {
        viewModel.startAttempt(TOKEN_ID)
        feed(chargeAuth(EVENT_ID), reject(EVENT_ID))

        assertEquals(TOKEN_ID, started.charge3dsId)
        assertEquals(TOKEN_ID, finished.charge3dsId)
    }

    @Test
    fun `with neither a token nor an event ID there is no charge ID on either`() {
        viewModel.startAttempt()
        feed(chargeAuth(null), success(null))

        assertNull(started.charge3dsId)
        assertNull(finished.charge3dsId)
    }

    // endregion

    // region finishes carry the charge ID and never a flow

    @Test
    fun `success, reject, additional data reject and cancel carry the charge ID and no flow`() {
        mapOf(
            success() to TelemetryEvent.PaymentOutcome.SUCCESS,
            reject() to TelemetryEvent.PaymentOutcome.DECLINED,
            additionalDataReject() to TelemetryEvent.PaymentOutcome.DECLINED,
            cancelled() to TelemetryEvent.PaymentOutcome.CANCELLED,
        ).forEach { (event, outcome) ->
            val reporting = RecordingTelemetry()
            MPGS3dsViewModel(dispatchersProvider, reporting).run {
                startAttempt()
                handleEventResult(Result.success(event))
            }

            val finish = reporting.paymentsFinished.single()
            assertEquals(outcome, finish.outcome, event.event.name)
            assertEquals(EVENT_ID, finish.charge3dsId, event.event.name)
            assertNull(finish.flow, event.event.name)
        }
    }

    @Test
    fun `a widget failure finish carries the charge ID and no flow`() {
        viewModel.startAttempt(TOKEN_ID)
        viewModel.failAttempt(MPGS3dsException.WebViewException(-1, "web view"))

        assertEquals(TelemetryEvent.PaymentOutcome.ERROR, finished.outcome)
        assertEquals(TOKEN_ID, finished.charge3dsId)
        assertNull(finished.flow)
    }

    @Test
    fun `a mapping failure finish carries the charge ID and no flow`() {
        viewModel.startAttempt()
        feed(chargeAuth())
        viewModel.handleEventResult(Result.failure(MPGS3dsException.EventMappingException("synthetic")))

        assertEquals(TelemetryEvent.PaymentOutcome.ERROR, finished.outcome)
        assertEquals(EVENT_ID, finished.charge3dsId)
        assertNull(finished.flow)
    }

    @Test
    fun `with telemetry inactive nothing is reported`() {
        val inactive = RecordingTelemetry(isActive = false)
        MPGS3dsViewModel(dispatchersProvider, inactive).run {
            startAttempt(TOKEN_ID)
            handleEventResult(Result.success(chargeAuth()))
            handleEventResult(Result.success(success()))
        }

        assertTrue(inactive.events.isEmpty())
    }

    // endregion

    private companion object {
        const val TOKEN_ID = "6741b9c2e4a17f0d3c5b8a9e"
        const val EVENT_ID = "5f2a7d0e6b348e41c2d73b9f"
        const val OTHER_ID = "0a9d5e72f348e7a3c9154d2b"
    }
}
