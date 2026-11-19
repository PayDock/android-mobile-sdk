@file:Suppress("TooManyFunctions") // the facade calls of the SDK, kept in one file

package com.paydock.core.telemetry

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryEvent
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import org.koin.core.context.GlobalContext
import java.util.Collections
import java.util.IdentityHashMap
import java.util.UUID
import java.util.concurrent.CancellationException

/*
 * The SDK's calls on the telemetry facade: one for a handled SDK error (and its variant for a report made inside a
 * coroutine), one for a widget opening, three for a payment attempt's start and end, and the one lookup of the facade
 * outside a view model. The calls return at once
 * when telemetry is inactive (NoOpTelemetry, bound after a failed or blank collector URL lookup), never throw and never
 * change the caller's control flow: a failing report is swallowed here.
 */

/**
 * Reports one SDK error (R_01 to R_03). Returns at once when ![Telemetry.isActive], and for a coroutine
 * cancellation: a java.util.concurrent.CancellationException that is not an SdkException, as the throwable or anywhere
 * along the throwable's or the cause's chain (research questions 14 and 16);
 * never throws; never changes the caller's control flow. The event is TelemetryEvent.SdkError(
 *   error = SdkErrorFacts.errorInfo(throwable, cause),
 *   errorCode = SdkErrorFacts.code(throwable),
 *   widget = widget,
 *   apiError = SdkErrorFacts.apiError(throwable, cause),
 * ) handed to report().
 *
 * @param throwable The error the merchant gets (or the one the path drops).
 * @param widget The [SdkWidget] name of the widget the path belongs to, or null outside a widget.
 * @param cause The original throwable [throwable] was built from, when the path still holds it.
 */
internal fun Telemetry.reportSdkError(throwable: Throwable, widget: String?, cause: Throwable? = null) {
    runCatching {
        if (!isActive || isCoroutineCancellation(throwable, cause)) return
        report(
            TelemetryEvent.SdkError(
                error = SdkErrorFacts.errorInfo(throwable, cause),
                errorCode = SdkErrorFacts.code(throwable),
                widget = widget,
                apiError = SdkErrorFacts.apiError(throwable, cause),
            )
        )
    }
}

/** Reports one widget opening (R_06, R_07): report(TelemetryEvent.WidgetOpened(widget)) when active; never throws. */
internal fun Telemetry.reportWidgetOpened(widget: String) {
    runCatching {
        if (!isActive) return
        report(TelemetryEvent.WidgetOpened(widget))
    }
}

/**
 * A new payment attempt (R_05, § 5.9): report(TelemetryEvent.PaymentStarted(widget, uuid)) and return the uuid, a
 * random java.util.UUID; null when inactive or on failure. Never throws.
 *
 * @param widget The [SdkWidget] name of the widget the attempt belongs to.
 * @return The attempt's payment ID, to pass to [finishPayment] or [failPayment]; null when nothing was reported.
 */
internal fun Telemetry.startPayment(widget: String): String? = runCatching {
    if (isActive) {
        UUID.randomUUID().toString().also { report(TelemetryEvent.PaymentStarted(widget, it)) }
    } else {
        null
    }
}.getOrNull()

/**
 * Ends [paymentId] (R_05): report(TelemetryEvent.PaymentFinished(widget, paymentId, outcome, errorCode)); a no-op
 * when [paymentId] is null or telemetry is inactive. The duration is set by the telemetry module, never here. Never
 * throws.
 *
 * @param widget The [SdkWidget] name of the widget the attempt belongs to.
 * @param paymentId The ID [startPayment] returned, or null when no attempt is open.
 * @param outcome How the attempt ended.
 * @param errorCode The code the merchant receives, kept only for [TelemetryEvent.PaymentOutcome.ERROR].
 */
internal fun Telemetry.finishPayment(
    widget: String,
    paymentId: String?,
    outcome: TelemetryEvent.PaymentOutcome,
    errorCode: String? = null,
) {
    runCatching {
        if (paymentId == null || !isActive) return
        report(TelemetryEvent.PaymentFinished(widget, paymentId, outcome, errorCode))
    }
}

/**
 * An error finish of [paymentId] with the code the merchant receives (R_07): errorCode = SdkErrorFacts.code(error),
 * none when [error] is null or not an SdkException. Never throws.
 *
 * @param widget The [SdkWidget] name of the widget the attempt belongs to.
 * @param paymentId The ID [startPayment] returned, or null when no attempt is open.
 * @param error The exception the merchant gets, or null when the path hands the merchant nothing.
 */
internal fun Telemetry.failPayment(widget: String, paymentId: String?, error: Throwable?) {
    runCatching {
        if (paymentId == null || !isActive) return
        finishPayment(widget, paymentId, TelemetryEvent.PaymentOutcome.ERROR, error?.let(SdkErrorFacts::code))
    }
}

/**
 * The same as [finishPayment], for a finish made inside a coroutine: returns at once when the calling coroutine is no
 * longer active (currentCoroutineContext().isActive is false), so the attempt is left open and counts as abandoned
 * (the merchant receives nothing from a cancelled coroutine). Never throws.
 */
internal suspend fun Telemetry.finishPaymentWhileActive(
    widget: String,
    paymentId: String?,
    outcome: TelemetryEvent.PaymentOutcome,
    errorCode: String? = null,
) {
    if (!currentCoroutineContext().isActive) return
    finishPayment(widget, paymentId, outcome, errorCode)
}

/**
 * The same as [failPayment], for a failure inside a coroutine: returns at once when the calling coroutine is no
 * longer active, leaving the attempt abandoned (R_07: the merchant receives no code). Never throws.
 */
internal suspend fun Telemetry.failPaymentWhileActive(widget: String, paymentId: String?, error: Throwable?) {
    if (!currentCoroutineContext().isActive) return
    failPayment(widget, paymentId, error)
}

/**
 * The same as reportSdkError, for a report made inside a coroutine: returns at once when the calling coroutine is no
 * longer active (currentCoroutineContext().isActive is false), because a mapped cancellation has lost its original
 * (research question 16). Every report that runs inside a coroutine uses it.
 *
 * @param throwable The error the merchant gets (or the one the path drops).
 * @param widget The [SdkWidget] name of the widget the path belongs to, or null outside a widget.
 * @param cause The original throwable [throwable] was built from, when the path still holds it.
 */
internal suspend fun Telemetry.reportSdkErrorWhileActive(
    throwable: Throwable,
    widget: String?,
    cause: Throwable? = null
) {
    if (!currentCoroutineContext().isActive) return
    reportSdkError(throwable, widget, cause)
}

/** The SDK's Telemetry facade from its global Koin, or null when there is none (previews, tests, SDK not started); never throws. */
internal fun telemetryOrNull(): Telemetry? = runCatching {
    GlobalContext.getOrNull()?.getOrNull<Telemetry>()
}.getOrNull()

/**
 * Whether [throwable] or [cause], or any throwable along either's chain, is a coroutine cancellation: a
 * [CancellationException] that is not an [SdkException].
 */
private fun isCoroutineCancellation(throwable: Throwable, cause: Throwable?): Boolean =
    (chainOf(throwable) + cause?.let(::chainOf).orEmpty()).any { it is CancellationException && it !is SdkException }

/** [throwable] and its causes, [TelemetryEvent.ErrorInfo.MAX_CAUSES] at most, stopping at a cause already seen. */
private fun chainOf(throwable: Throwable): List<Throwable> {
    val visited = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
    val chain = mutableListOf<Throwable>()
    var next: Throwable? = throwable
    while (next != null && chain.size <= TelemetryEvent.ErrorInfo.MAX_CAUSES && visited.add(next)) {
        chain.add(next)
        next = next.cause
    }
    return chain
}
