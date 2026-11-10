package com.paydock.core.telemetry

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryEvent
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import org.koin.core.context.GlobalContext
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.CancellationException

/*
 * The SDK's calls on the telemetry facade: one for a handled SDK error (and its variant for a report made inside a
 * coroutine), one for a widget opening, and the one lookup of the facade outside a view model. The calls return at once when telemetry is
 * inactive (a release build binds NoOpTelemetry), never throw and never change the caller's control flow: a failing
 * report is swallowed here.
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
