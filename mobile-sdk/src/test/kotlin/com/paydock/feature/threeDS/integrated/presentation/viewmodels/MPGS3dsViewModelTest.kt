package com.paydock.feature.threeDS.integrated.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.MPGS3dsException
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.WidgetSourceScan
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.threeDS.integrated.domain.model.integration.enums.MPGS3dsEventType
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsChargeEventData
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.MPGS3dsStatus
import com.paydock.feature.threeDS.integrated.presentation.state.MPGS3dsUIState
import com.paydock.telemetry.TelemetryEvent
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertSame
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
internal class MPGS3dsViewModelTest : BaseKoinUnitTest() {

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

    // region Telemetry: one SdkError per failure path (MP1-MP3), merchant-visible results unchanged

    @Test
    fun `event SdkException failure reports it once and still sets the error (MP1)`() = runTest {
        val failure = MPGS3dsException.EventMappingException("3DS Error has occurred!")

        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.failure(failure))

            awaitItem().let { state ->
                assertIs<MPGS3dsUIState.Error>(state)
                assertSame(failure, state.exception)
            }
        }
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.MPGS_3DS, reported.widget)
        assertEquals("EventMappingException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `event failure that is no SdkException reports it once and is still dropped (MP1)`() = runTest {
        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.failure(IllegalStateException()))

            expectNoEvents()
        }
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.MPGS_3DS, reported.widget)
        assertNull(reported.errorCode)
        assertEquals(IllegalStateException::class.java.name, reported.error.type)
    }

    @Test
    fun `a successful event reports nothing`() = runTest {
        val event = MPGS3dsEvent.ChargeAuthSuccessEvent(
            data = MPGS3dsChargeEventData(
                event = IntegratedEvent.CHARGE_AUTH_SUCCESS,
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = MPGS3dsStatus.AUTHENTICATED
            )
        )

        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.success(event))
            assertIs<MPGS3dsUIState.Success>(awaitItem())
        }
        assertEquals(emptyList<Any>(), telemetry.events.toList())
    }

    @Test
    fun `reportWidgetError reports one MPGS 3DS SdkError and emits no state (MP3)`() = runTest {
        viewModel.eventFlow.test {
            viewModel.reportWidgetError(MPGS3dsException.WebViewException(-1, "web view"))

            expectNoEvents()
        }
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.MPGS_3DS, reported.widget)
        assertEquals("WebViewException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `reportWidgetError reports only the first web view failure per view model (MP3)`() = runTest {
        viewModel.eventFlow.test {
            // Child windows can fire one error per failed resource.
            viewModel.reportWidgetError(MPGS3dsException.WebViewException(-1, "web view"))
            viewModel.reportWidgetError(MPGS3dsException.WebViewException(-2, "web view"))

            expectNoEvents()
        }
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.MPGS_3DS, reported.widget)
        assertEquals("WebViewException", reported.errorCode)
    }

    @Test
    fun `the invalid token returns report from an effect before the early return, once per failure type (MP2)`() {
        val source = WidgetSourceScan.read("feature/threeDS/integrated/presentation/MPGS3dsWidget.kt")
        val widget = WidgetSourceScan.function(source, "MPGS3dsWidget")
        val invalidToken = WidgetSourceScan.between(widget, "if (parsedToken == null) {", "} else if")
        val invalidFormat = WidgetSourceScan.between(widget, "} else if", "koinViewModel()")
        val reporter = WidgetSourceScan.function(WidgetSourceScan.read("core/telemetry/ReportWidgetOpened.kt"), "ReportSdkErrorOnce")

        listOf(invalidToken, invalidFormat).forEach { branch ->
            assertEquals(1, WidgetSourceScan.count(branch, "MPGS3dsException.InvalidTokenException("))
            assertEquals(1, WidgetSourceScan.count(branch, "ReportSdkErrorOnce(failure, SdkWidget.MPGS_3DS)"))
            assertEquals(1, WidgetSourceScan.count(branch, "ReportPaymentFailedOnce(SdkWidget.MPGS_3DS, failure)"))
            assertEquals(1, WidgetSourceScan.count(branch, "completion(Result.failure(failure))"))
            assertTrue(
                branch.indexOf("ReportSdkErrorOnce(failure, SdkWidget.MPGS_3DS)") < branch.indexOf("return"),
                "the report precedes the early return"
            )
        }
        assertEquals(1, WidgetSourceScan.count(reporter, "remember { telemetryOrNull() }"))
        assertEquals(1, WidgetSourceScan.count(reporter, "LaunchedEffect(error::class)"))
        assertEquals(1, WidgetSourceScan.count(reporter, "telemetry?.reportSdkError(error, widget)"))
    }

    @Test
    fun `the web view error handler reports the WebViewException it hands on, once (MP3)`() {
        val source = WidgetSourceScan.read("feature/threeDS/integrated/presentation/MPGS3dsWidget.kt")
        val onError = WidgetSourceScan.between(source, "{ status, message ->", "completion(Result.failure(failure))")

        assertEquals(1, WidgetSourceScan.count(source, "viewModel.reportWidgetError("))
        assertEquals(1, WidgetSourceScan.count(onError, "MPGS3dsException.WebViewException("))
        assertEquals(1, WidgetSourceScan.count(onError, "viewModel.reportWidgetError(failure)"))
        assertEquals(1, WidgetSourceScan.count(onError, "viewModel.failAttempt(failure)"))
    }

    // endregion

    // region Telemetry: one payment attempt per view model (§ 5.9, R_06 to R_08)

    private fun event(type: IntegratedEvent): MPGS3dsEvent {
        val data = MPGS3dsChargeEventData(
            event = type,
            charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
            status = MPGS3dsStatus.AUTHENTICATED
        )
        return when (type) {
            IntegratedEvent.CHARGE_AUTH_SUCCESS -> MPGS3dsEvent.ChargeAuthSuccessEvent(data = data)
            IntegratedEvent.CHARGE_AUTH_REJECT -> MPGS3dsEvent.ChargeAuthRejectEvent(data = data)
            IntegratedEvent.CHARGE_AUTH_CANCELLED -> MPGS3dsEvent.ChargeAuthCancelledEvent(data = data)
            IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS -> MPGS3dsEvent.AdditionalDataCollectSuccessEvent(data = data)
            IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT -> MPGS3dsEvent.AdditionalDataCollectRejectEvent(data = data)
            IntegratedEvent.CHARGE_AUTH -> MPGS3dsEvent.ChargeAuthEvent(data = data)
        }
    }

    @Test
    fun `startAttempt twice starts one attempt`() {
        viewModel.startAttempt()
        viewModel.startAttempt()

        assertEquals(SdkWidget.MPGS_3DS, telemetry.paymentsStarted.single().widget)
    }

    @Test
    fun `each terminal event finishes the attempt once with its outcome, and the others finish nothing`() {
        val expected = mapOf(
            IntegratedEvent.CHARGE_AUTH_SUCCESS to TelemetryEvent.PaymentOutcome.SUCCESS,
            IntegratedEvent.CHARGE_AUTH_REJECT to TelemetryEvent.PaymentOutcome.DECLINED,
            IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT to TelemetryEvent.PaymentOutcome.DECLINED,
            IntegratedEvent.CHARGE_AUTH_CANCELLED to TelemetryEvent.PaymentOutcome.CANCELLED,
            IntegratedEvent.CHARGE_AUTH to null,
            IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS to null,
        )
        assertEquals(IntegratedEvent.entries.toSet(), expected.keys)
        expected.forEach { (type, outcome) ->
            val reporting = RecordingTelemetry()
            MPGS3dsViewModel(dispatchersProvider, reporting).run {
                startAttempt()
                handleEventResult(Result.success(event(type)))
                handleEventResult(Result.success(event(IntegratedEvent.CHARGE_AUTH_SUCCESS)))
            }

            val first = reporting.paymentsFinished.first()
            assertEquals(reporting.paymentsStarted.single().paymentId, first.paymentId)
            assertEquals(outcome ?: TelemetryEvent.PaymentOutcome.SUCCESS, first.outcome, type.name)
            assertEquals(1, reporting.paymentsFinished.size, type.name)
            assertNull(first.errorCode)
        }
    }

    @Test
    fun `a mapping failure finishes as error with its code, or none when it is no SdkException`() {
        viewModel.startAttempt()
        viewModel.handleEventResult(Result.failure(MPGS3dsException.EventMappingException("synthetic")))
        val other = RecordingTelemetry()
        MPGS3dsViewModel(dispatchersProvider, other).run {
            startAttempt()
            handleEventResult(Result.failure(IllegalStateException()))
        }

        val finished = telemetry.paymentsFinished.single()
        assertEquals(TelemetryEvent.PaymentOutcome.ERROR, finished.outcome)
        assertEquals("EventMappingException", finished.errorCode)
        assertEquals(TelemetryEvent.PaymentOutcome.ERROR, other.paymentsFinished.single().outcome)
        assertNull(other.paymentsFinished.single().errorCode)
    }

    @Test
    fun `a web view error finishes as error with the WebViewException code and emits no state`() = runTest {
        viewModel.eventFlow.test {
            viewModel.startAttempt()
            viewModel.failAttempt(MPGS3dsException.WebViewException(-1, "web view"))
            viewModel.failAttempt(MPGS3dsException.WebViewException(-2, "web view"))

            expectNoEvents()
        }
        val finished = telemetry.paymentsFinished.single()
        assertEquals(TelemetryEvent.PaymentOutcome.ERROR, finished.outcome)
        assertEquals("WebViewException", finished.errorCode)
    }

    @Test
    fun `with inactive telemetry the same states are emitted and nothing is reported`() = runTest {
        val inactive = RecordingTelemetry(isActive = false)
        val quiet = MPGS3dsViewModel(dispatchersProvider, inactive)
        val failure = MPGS3dsException.EventMappingException("synthetic")

        quiet.eventFlow.test {
            quiet.startAttempt()
            quiet.handleEventResult(Result.success(event(IntegratedEvent.CHARGE_AUTH_SUCCESS)))
            // The view model's own initial Idle may still be on its way.
            awaitItem().let { if (it is MPGS3dsUIState.Idle) awaitItem() else it }.let { state ->
                assertIs<MPGS3dsUIState.Success>(state)
                assertEquals(MPGS3dsEventType.CHARGE_AUTH_SUCCESS, state.result.event)
            }
            quiet.handleEventResult(Result.failure(failure))
            awaitItem().let { state ->
                assertIs<MPGS3dsUIState.Error>(state)
                assertSame(failure, state.exception)
            }
            quiet.failAttempt(MPGS3dsException.WebViewException(-1, "web view"))
            expectNoEvents()
        }
        assertTrue(inactive.events.isEmpty())
    }

    @Test
    fun `the widget starts its attempt from its launch effect`() {
        val source = WidgetSourceScan.read("feature/threeDS/integrated/presentation/MPGS3dsWidget.kt")
        val launch = WidgetSourceScan.between(source, "LaunchedEffect(Unit) {", "viewModel.eventFlow.collect")

        assertEquals(1, WidgetSourceScan.count(source, "viewModel.startAttempt()"))
        assertEquals(1, WidgetSourceScan.count(launch, "viewModel.startAttempt()"))
    }

    // endregion
}
