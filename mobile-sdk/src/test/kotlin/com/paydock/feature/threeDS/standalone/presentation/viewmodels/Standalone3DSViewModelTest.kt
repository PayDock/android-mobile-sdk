package com.paydock.feature.threeDS.standalone.presentation.viewmodels

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.Standalone3DSException
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.WidgetSourceScan
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
import com.paydock.feature.threeDS.standalone.domain.utils.UnknownStandalone3DSEventException
import com.paydock.feature.threeDS.standalone.presentation.state.Standalone3DSUIState
import com.paydock.feature.threeDS.standalone.presentation.utils.Standalone3DSJSBridge
import com.paydock.feature.threeDS.standalone.presentation.utils.Standalone3DSLoadingController
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
import kotlin.test.assertSame
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
internal class Standalone3DSViewModelTest : BaseKoinUnitTest() {

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

    private suspend fun ReceiveTurbine<Standalone3DSUIState>.awaitSuccess(
        eventType: StandaloneEventType,
        charge3dsId: String? = chargeId,
        description: String? = null,
        authenticationFlow: Standalone3DSAuthenticationFlow? = null
    ) {
        awaitItem().let { state ->
            assertIs<Standalone3DSUIState.Success>(state)
            assertEquals(
                Standalone3DSResult(
                    eventType,
                    charge3dsId,
                    resultDescription = description,
                    authenticationFlow = authenticationFlow
                ),
                state.result
            )
        }
    }

    @Test
    fun `handleEventResult with chargeAuthSuccess event should finish loading then update event state`() = runTest {
        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.success(successEvent))

            assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
            awaitSuccess(
                StandaloneEventType.CHARGE_AUTH_SUCCESS,
                authenticationFlow = Standalone3DSAuthenticationFlow.FRICTIONLESS
            )
            expectNoEvents()
        }
    }

    @Test
    fun `handleEventResult with chargeAuthReject event should finish loading then update event state`() = runTest {
        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.success(rejectEvent))

            assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
            awaitSuccess(
                StandaloneEventType.CHARGE_AUTH_REJECT,
                authenticationFlow = Standalone3DSAuthenticationFlow.FRICTIONLESS
            )
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

    // region Telemetry: one SdkError per failure path (SD1-SD5), merchant-visible results unchanged

    @Test
    fun `event SdkException failure reports it once and still finishes loading and sets the error (SD1)`() = runTest {
        val failure = Standalone3DSException.EventMappingException("3DS Error has occurred!")

        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.failure(failure))

            assertEquals(Standalone3DSUIState.Loading(false), awaitItem())
            awaitItem().let { state ->
                assertIs<Standalone3DSUIState.Error>(state)
                assertSame(failure, state.exception)
            }
            expectNoEvents()
        }
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.STANDALONE_3DS, reported.widget)
        assertEquals("EventMappingException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `event failure that is no SdkException reports it once and is still dropped (SD1)`() = runTest {
        viewModel.eventFlow.test {
            viewModel.handleEventResult(Result.failure(IllegalStateException()))

            expectNoEvents()
        }
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.STANDALONE_3DS, reported.widget)
        assertNull(reported.errorCode)
        assertEquals(IllegalStateException::class.java.name, reported.error.type)
    }

    @Test
    fun `a successful event reports nothing`() = runTest {
        collectStates(successEvent)

        assertEquals(emptyList<Any>(), telemetry.events.toList())
    }

    @Test
    fun `an unknown bridge event reports one SdkError through the view model and emits no state (SD2)`() = runTest {
        val bridge = Standalone3DSJSBridge(
            eventCallback = { eventResult -> viewModel.handleEventResult(eventResult) },
            onUnknownEvent = { unknownEvent -> viewModel.reportWidgetError(unknownEvent) },
        )

        viewModel.eventFlow.test {
            bridge.postMessage("{\"event\":\"unknownEvent\",\"data\":{\"status\":\"pending\"}}")

            expectNoEvents()
        }
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.STANDALONE_3DS, reported.widget)
        assertNull(reported.errorCode)
        assertEquals(UnknownStandalone3DSEventException::class.java.name, reported.error.type)
    }

    @Test
    fun `reportWidgetError reports one Standalone 3DS SdkError and emits no state (SD4, SD5)`() = runTest {
        viewModel.eventFlow.test {
            viewModel.reportWidgetError(
                Standalone3DSException.RenderProcessGoneException(didCrash = true, displayableMessage = "gone")
            )

            expectNoEvents()
        }
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.STANDALONE_3DS, reported.widget)
        assertEquals("RenderProcessGoneException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `the widget hands unknown bridge events to the view model's reportWidgetError (SD2)`() {
        val source = WidgetSourceScan.read("feature/threeDS/standalone/presentation/Standalone3DSWidget.kt")
        val bridge = WidgetSourceScan.between(source, "Standalone3DSJSBridge(", "Box(")

        val handOff = "onUnknownEvent = { unknownEvent -> viewModel.reportWidgetError(unknownEvent) }"

        assertEquals(1, WidgetSourceScan.count(bridge, handOff))
    }

    @Test
    fun `the invalid token returns report from an effect before the early return, once per failure type (SD3)`() {
        val source = WidgetSourceScan.read("feature/threeDS/standalone/presentation/Standalone3DSWidget.kt")
        val widget = WidgetSourceScan.function(source, "Standalone3DSWidget")
        val invalidToken = WidgetSourceScan.between(widget, "if (parsedToken == null) {", "} else if")
        val invalidFormat = WidgetSourceScan.between(widget, "} else if", "koinViewModel()")
        val reporter = WidgetSourceScan.function(WidgetSourceScan.read("core/telemetry/ReportWidgetOpened.kt"), "ReportSdkErrorOnce")

        listOf(invalidToken, invalidFormat).forEach { branch ->
            assertEquals(1, WidgetSourceScan.count(branch, "Standalone3DSException.InvalidTokenException("))
            assertEquals(1, WidgetSourceScan.count(branch, "ReportSdkErrorOnce(failure, SdkWidget.STANDALONE_3DS)"))
            assertEquals(1, WidgetSourceScan.count(branch, "ReportPaymentFailedOnce(SdkWidget.STANDALONE_3DS, failure)"))
            assertEquals(1, WidgetSourceScan.count(branch, "completion(Result.failure(failure))"))
            assertTrue(
                branch.indexOf("ReportSdkErrorOnce(failure, SdkWidget.STANDALONE_3DS)") < branch.indexOf("return"),
                "the report precedes the early return"
            )
        }
        assertEquals(1, WidgetSourceScan.count(reporter, "remember { telemetryOrNull() }"))
        assertEquals(1, WidgetSourceScan.count(reporter, "LaunchedEffect(error::class)"))
        assertEquals(1, WidgetSourceScan.count(reporter, "telemetry?.reportSdkError(error, widget)"))
    }

    @Test
    fun `the render process gone and web view error handlers report what they hand on, once each (SD4, SD5)`() {
        val source = WidgetSourceScan.read("feature/threeDS/standalone/presentation/Standalone3DSWidget.kt")
        val rendererGone =
            WidgetSourceScan.between(source, "onRendererGone = { didCrash ->", "{ status, message, host ->")
        val webView = WidgetSourceScan.between(source, "{ status, message, host ->", "SdkOverlayLoader(")

        assertEquals(3, WidgetSourceScan.count(source, "viewModel.reportWidgetError("))
        assertEquals(1, WidgetSourceScan.count(rendererGone, "Standalone3DSException.RenderProcessGoneException("))
        assertEquals(1, WidgetSourceScan.count(rendererGone, "viewModel.reportWidgetError(failure)"))
        assertEquals(1, WidgetSourceScan.count(rendererGone, "viewModel.failAttempt(failure)"))
        assertEquals(1, WidgetSourceScan.count(rendererGone, "currentCompletion(Result.failure(failure))"))
        assertEquals(1, WidgetSourceScan.count(webView, "Standalone3DSException.WebViewException("))
        assertEquals(1, WidgetSourceScan.count(webView, "viewModel.reportWidgetError(failure)"))
        assertEquals(1, WidgetSourceScan.count(webView, "viewModel.failAttempt(failure)"))
        assertEquals(1, WidgetSourceScan.count(webView, "currentCompletion(Result.failure(failure))"))
    }

    // endregion

    // region Telemetry: one payment attempt per view model (§ 5.9, R_06 to R_08)

    private fun finishOf(vararg events: Standalone3DSEvent, reporting: RecordingTelemetry = telemetry) =
        Standalone3DSViewModel(dispatchersProvider, reporting).run {
            startAttempt()
            events.forEach { handleEventResult(Result.success(it)) }
            reporting.paymentsFinished
        }

    @Test
    fun `startAttempt twice starts one attempt`() {
        viewModel.startAttempt()
        viewModel.startAttempt()

        assertEquals(SdkWidget.STANDALONE_3DS, telemetry.paymentsStarted.single().widget)
    }

    @Test
    fun `each terminal event finishes the attempt once with its outcome`() {
        val expected = listOf(
            successEvent to TelemetryEvent.PaymentOutcome.SUCCESS,
            rejectEvent to TelemetryEvent.PaymentOutcome.DECLINED,
            errorEvent to TelemetryEvent.PaymentOutcome.ERROR,
        )
        expected.forEach { (event, outcome) ->
            val reporting = RecordingTelemetry()

            val finished = finishOf(event, successEvent, reporting = reporting).single()

            assertEquals(reporting.paymentsStarted.single().paymentId, finished.paymentId)
            assertEquals(SdkWidget.STANDALONE_3DS, finished.widget)
            assertEquals(outcome, finished.outcome)
            assertEquals(if (event == errorEvent) "CHARGE_ERROR" else null, finished.errorCode)
        }
    }

    @Test
    fun `challenge, decoupled, info and challenge loaded or completed send no finish`() {
        val finished =
            finishOf(challengeEvent, challengeLoadedEvent, challengeCompletedEvent, decoupledEvent, infoEvent)

        assertTrue(finished.isEmpty())
        assertEquals(1, telemetry.paymentsStarted.size)
    }

    @Test
    fun `a mapping failure finishes as error with its code, or none when it is no SdkException`() {
        viewModel.startAttempt()
        viewModel.handleEventResult(Result.failure(Standalone3DSException.EventMappingException("synthetic")))
        val other = RecordingTelemetry()
        Standalone3DSViewModel(dispatchersProvider, other).run {
            startAttempt()
            handleEventResult(Result.failure(IllegalStateException()))
        }

        assertEquals("EventMappingException", telemetry.paymentsFinished.single().errorCode)
        assertEquals(TelemetryEvent.PaymentOutcome.ERROR, other.paymentsFinished.single().outcome)
        assertNull(other.paymentsFinished.single().errorCode)
    }

    @Test
    fun `render process gone and web view errors finish as error with their codes`() = runTest {
        viewModel.eventFlow.test {
            viewModel.startAttempt()
            viewModel.failAttempt(
                Standalone3DSException.RenderProcessGoneException(didCrash = true, displayableMessage = "gone")
            )
            viewModel.failAttempt(Standalone3DSException.WebViewException(code = -1, displayableMessage = "web view"))

            expectNoEvents()
        }
        val other = RecordingTelemetry()
        Standalone3DSViewModel(dispatchersProvider, other).run {
            startAttempt()
            failAttempt(Standalone3DSException.WebViewException(code = -1, displayableMessage = "web view"))
        }

        assertEquals("RenderProcessGoneException", telemetry.paymentsFinished.single().errorCode)
        assertEquals("WebViewException", other.paymentsFinished.single().errorCode)
    }

    @Test
    fun `with inactive telemetry the same states are emitted and nothing is reported`() = runTest {
        val inactive = RecordingTelemetry(isActive = false)
        val quiet = Standalone3DSViewModel(dispatchersProvider, inactive)
        val states = mutableListOf<Standalone3DSUIState>()
        quiet.eventFlow.test {
            quiet.startAttempt()
            listOf(challengeEvent, successEvent, errorEvent).forEach { quiet.handleEventResult(Result.success(it)) }
            quiet.handleEventResult(Result.failure(Standalone3DSException.EventMappingException("synthetic")))
            quiet.failAttempt(Standalone3DSException.WebViewException(code = -1, displayableMessage = "web view"))
            states += cancelAndConsumeRemainingEvents()
                .filterIsInstance<app.cash.turbine.Event.Item<Standalone3DSUIState>>()
                .map { it.value }
        }

        val expected = collectStates(challengeEvent, successEvent, errorEvent) +
            listOf(Standalone3DSUIState.Loading(false), states.last())
        assertEquals(expected, states)
        assertIs<Standalone3DSUIState.Error>(states.last())
        assertTrue(inactive.events.isEmpty())
    }

    @Test
    fun `the widget starts its attempt from its launch effect and the invalid token helper starts and fails one`() {
        val source = WidgetSourceScan.read("feature/threeDS/standalone/presentation/Standalone3DSWidget.kt")
        val launch = WidgetSourceScan.between(source, "LaunchedEffect(Unit) {", "viewModel.eventFlow.collect")
        val helper = WidgetSourceScan.function(
            WidgetSourceScan.read("core/telemetry/ReportWidgetOpened.kt"),
            "ReportPaymentFailedOnce"
        )

        assertEquals(1, WidgetSourceScan.count(source, "viewModel.startAttempt()"))
        assertEquals(1, WidgetSourceScan.count(launch, "viewModel.startAttempt()"))
        assertEquals(1, WidgetSourceScan.count(helper, "LaunchedEffect(error::class)"))
        assertEquals(1, WidgetSourceScan.count(helper, "it.failPayment(widget, it.startPayment(widget), error)"))
    }

    // endregion

    // region Authentication flow per charge ID (MD-56, R_02 to R_06)

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

    // endregion

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
        const val CHARGE_A = "charge-a"
        const val CHARGE_B = "charge-b"
    }
}
