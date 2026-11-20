package com.paydock.core.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Source scan for the payment journeys (R_06, § 5.9): each of the five paying widgets starts an attempt at exactly
 * one site, `startPayment(` in its view model (and, for the 3DS widgets, `ReportPaymentFailedOnce(` in each
 * invalid-token return), and each path that ends an attempt holds a finish call. The view model tests drive the
 * paths; this scan keeps a new ending path from slipping in without its finish.
 */
internal class PaymentJourneyScanTest {

    private val cardVm = "feature/card/presentation/viewmodels/CardDetailsViewModel.kt"
    private val googlePayVm = "feature/googlepay/presentation/viewmodels/GooglePayViewModel.kt"
    private val googlePayWidget = "feature/googlepay/presentation/GooglePayWidget.kt"
    private val standaloneVm = "feature/threeDS/standalone/presentation/viewmodels/Standalone3DSViewModel.kt"
    private val standaloneWidget = "feature/threeDS/standalone/presentation/Standalone3DSWidget.kt"
    private val mpgsVm = "feature/threeDS/integrated/presentation/viewmodels/MPGS3dsViewModel.kt"
    private val mpgsWidget = "feature/threeDS/integrated/presentation/MPGS3dsWidget.kt"
    private val payPalVm = "feature/paypal/checkout/presentation/viewmodel/PayPalViewModel.kt"
    private val payPalWidget = "feature/paypal/checkout/presentation/PayPalWidget.kt"

    private fun read(path: String) = WidgetSourceScan.read(path)

    private fun count(path: String, call: String) = WidgetSourceScan.count(read(path), call)

    @Test
    fun eachPayingViewModel_startsAnAttemptAtExactlyOneSite_forItsOwnWidget() {
        mapOf(
            cardVm to "SdkWidget.CARD_DETAILS)",
            googlePayVm to "SdkWidget.GOOGLE_PAY)",
            standaloneVm to "SdkWidget.STANDALONE_3DS, chargeId)",
            mpgsVm to "SdkWidget.MPGS_3DS, chargeId)",
            payPalVm to "SdkWidget.PAYPAL)",
        ).forEach { (path, arguments) ->
            assertEquals(path, 1, count(path, "startPayment("))
            assertEquals(path, 1, count(path, "telemetry.startPayment($arguments"))
        }
    }

    @Test
    fun theOtherPayingSources_startNoAttemptOfTheirOwn() {
        listOf(googlePayWidget, standaloneWidget, mpgsWidget, payPalWidget).forEach { path ->
            assertEquals(path, 0, count(path, "startPayment("))
        }
        listOf(
            "core/presentation/viewmodels/BaseViewModel.kt",
            "feature/wallet/presentation/viewmodels/WalletViewModel.kt"
        ).forEach { path -> assertEquals(path, 0, count(path, "Payment(")) }
    }

    @Test
    fun the3dsInvalidTokenReturns_eachStartAndFailOneAttempt() {
        val mpgsStart = "viewModel.startAttempt(ThreeDSTokenUtils.extractCharge3dsId(config.token))"
        mapOf(
            standaloneWidget to ("SdkWidget.STANDALONE_3DS" to "viewModel.startAttempt(charge3dsId)"),
            mpgsWidget to ("SdkWidget.MPGS_3DS" to mpgsStart),
        ).forEach { (path, expected) ->
            val (widget, start) = expected
            assertEquals(path, 2, count(path, "ReportPaymentFailedOnce($widget, failure)"))
            assertEquals(path, 1, count(path, "viewModel.startAttempt("))
            assertEquals(path, 1, count(path, start))
        }
    }

    @Test
    fun card_finishesOnSuccessAndOnEveryFailure() {
        val tokenise = WidgetSourceScan.between(read(cardVm), "fun tokeniseCard()", "private suspend fun finishAttempt(")
        val onSuccess = WidgetSourceScan.between(tokenise, ".onSuccess", ".onFailure")
        val onFailure = tokenise.substring(tokenise.indexOf(".onFailure"))

        assertEquals(1, WidgetSourceScan.count(onSuccess, "finishAttempt(TelemetryEvent.PaymentOutcome.SUCCESS)"))
        assertEquals(1, WidgetSourceScan.count(onFailure, "finishAttempt(TelemetryEvent.PaymentOutcome.ERROR, error)"))
    }

    @Test
    fun googlePay_finishesOnEveryStateUpdate_andOnTheWidgetsNoDataBranches() {
        val update =
            WidgetSourceScan.between(read(googlePayVm), "private fun updateUiState(", "private fun finishAttempt(")
        val widget = read(googlePayWidget)
        val resolvable = WidgetSourceScan.function(widget, "handleResolvableResult")
        val taskResult = WidgetSourceScan.function(widget, "handleGooglePayResult")

        assertEquals(1, WidgetSourceScan.count(update, "finishPaymentOn(newState)"))
        assertEquals(1, WidgetSourceScan.count(update, "is GooglePayUIState.Success ->"))
        assertEquals(1, WidgetSourceScan.count(update, "is GooglePayUIState.Error ->"))
        assertEquals(2, WidgetSourceScan.count(resolvable, "?: viewModel.handleMissingPaymentData()"))
        assertEquals(1, WidgetSourceScan.count(taskResult, "?: viewModel.handleMissingPaymentData()"))
    }

    @Test
    fun standalone3ds_finishesOnItsFinalEvents_mappingFailure_andTheWidgetsErrorBranches() {
        val vm = read(standaloneVm)
        val events = WidgetSourceScan.between(vm, "private fun finishPaymentOn(", "private fun finishAttempt(")

        listOf("ChargeAuthSuccessEvent ->", "ChargeAuthRejectEvent ->", "ChargeErrorEvent ->").forEach {
            assertEquals(it, 1, WidgetSourceScan.count(events, it))
        }
        assertEquals(1, WidgetSourceScan.count(vm, "finishPaymentOn(event, result?.authenticationFlow)"))
        val onFailure = WidgetSourceScan.between(vm, "onFailure = {", "fun reportWidgetError(")
        assertEquals(1, WidgetSourceScan.count(onFailure, "failAttempt(throwable)"))
        assertEquals(2, count(standaloneWidget, "viewModel.failAttempt(failure)"))
    }

    @Test
    fun mpgs3ds_finishesOnItsFinalEvents_mappingFailure_andTheWebViewError() {
        val vm = read(mpgsVm)
        val events = WidgetSourceScan.between(vm, "private fun finishPaymentOn(", "private fun finishAttempt(")

        listOf(
            "ChargeAuthSuccessEvent ->",
            "AdditionalDataCollectRejectEvent ->",
            "ChargeAuthCancelledEvent ->",
        ).forEach { assertEquals(it, 1, WidgetSourceScan.count(events, it)) }
        assertEquals(1, WidgetSourceScan.count(events, "is MPGS3dsEvent.ChargeAuthRejectEvent,"))
        assertEquals(1, WidgetSourceScan.count(vm, "finishPaymentOn(event)"))
        assertEquals(1, WidgetSourceScan.count(vm, "failAttempt(throwable)"))
        assertEquals(1, count(mpgsWidget, "viewModel.failAttempt(failure)"))
    }

    @Test
    fun payPal_finishesOnCaptureSuccess_everyFailurePath_andEveryWebResultEnd() {
        val vm = read(payPalVm)
        val handler = WidgetSourceScan.function(read(payPalWidget), "handlePayPalResult")

        assertEquals(1, WidgetSourceScan.count(vm, "finishAttempt(TelemetryEvent.PaymentOutcome.SUCCESS)"))
        // Order id, wallet callback, capture, client id and lost wallet token; the merchant token failure by its code.
        val failureFinishes = WidgetSourceScan.count(vm, "failAttempt(failure)") +
            WidgetSourceScan.count(vm, "failAttempt(mapped)")
        assertEquals(5, failureFinishes)
        val merchantTokenCode = "errorCode = PayPalException.InitialisationWalletTokenException"
        assertEquals(1, WidgetSourceScan.count(vm, merchantTokenCode))
        // Every error state the view model sets is one of those six ends.
        assertEquals(6, WidgetSourceScan.count(vm, "PayPalCheckoutUIState.Error("))
        assertEquals(1, WidgetSourceScan.count(handler, "viewModel.cancelAttempt()"))
        assertEquals(1, WidgetSourceScan.count(handler, "viewModel.failAttempt(failure)"))
        assertEquals(3, WidgetSourceScan.count(handler, "viewModel.failAttempt()"))
        val cancellation = handler.indexOf("PayPalException.CancellationException(")
        assertTrue(handler.indexOf("viewModel.cancelAttempt()") < cancellation)
    }
}
