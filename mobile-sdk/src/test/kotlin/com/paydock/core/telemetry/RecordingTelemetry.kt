package com.paydock.core.telemetry

import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryEvent
import java.util.Collections

/**
 * A [Telemetry] facade for tests that records every reported event, in order. With `isActive = false` it stands for
 * the release binding: the SDK's calls must then never reach [report], so [events] stays empty.
 */
internal class RecordingTelemetry(override val isActive: Boolean = true) : Telemetry {

    /** Every event handed to [report], in order. */
    val events: MutableList<TelemetryEvent> = Collections.synchronizedList(mutableListOf())

    /** The [TelemetryEvent.SdkError]s reported so far, in order. */
    val sdkErrors: List<TelemetryEvent.SdkError>
        get() = synchronized(events) { events.filterIsInstance<TelemetryEvent.SdkError>() }

    /** The [TelemetryEvent.WidgetOpened]s reported so far, in order. */
    val widgetsOpened: List<TelemetryEvent.WidgetOpened>
        get() = synchronized(events) { events.filterIsInstance<TelemetryEvent.WidgetOpened>() }

    /** The [TelemetryEvent.PaymentStarted]s reported so far, in order. */
    val paymentsStarted: List<TelemetryEvent.PaymentStarted>
        get() = synchronized(events) { events.filterIsInstance<TelemetryEvent.PaymentStarted>() }

    /** The [TelemetryEvent.PaymentFinished]es reported so far, in order. */
    val paymentsFinished: List<TelemetryEvent.PaymentFinished>
        get() = synchronized(events) { events.filterIsInstance<TelemetryEvent.PaymentFinished>() }

    override fun report(event: TelemetryEvent) {
        events += event
    }
}
