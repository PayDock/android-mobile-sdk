package com.paydock.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract tests for the telemetry surface: the facade and the lifecycle handle are separate, the facade only
 * reports events, and events and intents are disjoint families.
 *
 * Subclasses are listed explicitly: `sealedSubclasses` needs kotlin-reflect, which is not on the classpath.
 */
internal class TelemetryContractTest {

    private val eventSubclasses: List<Class<*>> = listOf(
        TelemetryEvent.Crash::class.java,
        TelemetryEvent.UncaughtException::class.java,
        TelemetryEvent.SdkError::class.java,
        TelemetryEvent.NetworkFailure::class.java
    )

    /** Every class nested in [TelemetryEvent], at any depth. */
    private val nestedEventClasses: List<Class<*>> = listOf(
        TelemetryEvent.Crash::class.java,
        TelemetryEvent.UncaughtException::class.java,
        TelemetryEvent.SdkError::class.java,
        TelemetryEvent.NetworkFailure::class.java,
        TelemetryEvent.ErrorInfo::class.java,
        TelemetryEvent.ErrorInfo.Companion::class.java,
        TelemetryEvent.NetworkError::class.java,
        TelemetryEvent.NetworkError.HttpStatus::class.java,
        TelemetryEvent.NetworkError.Transport::class.java,
        TelemetryEvent.NetworkError.Decode::class.java,
        TelemetryEvent.ApiError::class.java,
        TelemetryEvent.CrashSource::class.java,
        TelemetryEvent.TransportKind::class.java,
        TelemetryEvent.HttpMethod::class.java
    )

    private val intentSubclasses: List<Class<*>> = listOf(
        TelemetryIntent.Reported::class.java,
        TelemetryIntent.Flush::class.java,
        TelemetryIntent.AppBackgrounded::class.java,
        TelemetryIntent.LevelChanged::class.java
    )

    @Test
    fun noOp_isBothFacadeAndRuntime() {
        assertTrue(Telemetry::class.java.isAssignableFrom(NoOpTelemetry::class.java))
        assertTrue(TelemetryRuntime::class.java.isAssignableFrom(NoOpTelemetry::class.java))
    }

    @Test
    fun active_isBothFacadeAndRuntime() {
        assertTrue(Telemetry::class.java.isAssignableFrom(ActiveTelemetry::class.java))
        assertTrue(TelemetryRuntime::class.java.isAssignableFrom(ActiveTelemetry::class.java))
    }

    @Test
    fun facade_declaresExactlyIsActiveAndReport() {
        val names = Telemetry::class.java.declaredMethods
            .filterNot { it.isSynthetic }
            .map { it.name }
            .sorted()

        assertEquals(listOf("isActive", "report"), names)
    }

    @Test
    fun facade_isNotRuntime() {
        assertFalse(TelemetryRuntime::class.java.isAssignableFrom(Telemetry::class.java))
    }

    @Test
    fun reportedIntent_wrapsAnEvent() {
        val error = TelemetryEvent.ErrorInfo.from(IllegalStateException("synthetic failure"))
        val event = TelemetryEvent.SdkError(error = error)

        val intent = TelemetryIntent.Reported(event)

        assertSame(event, intent.event)
        assertEquals(TelemetryEvent::class.java, TelemetryIntent.Reported::class.java.getDeclaredField("event").type)
    }

    @Test
    fun levelChangedIntent_carriesTheLevel() {
        assertEquals(TelemetryLevel.ERRORS, TelemetryIntent.LevelChanged(TelemetryLevel.ERRORS).level)
    }

    @Test
    fun noEventIsAnIntent() {
        eventSubclasses.forEach { event ->
            assertTrue(TelemetryEvent::class.java.isAssignableFrom(event))
            assertFalse("${event.name} must not be an intent", TelemetryIntent::class.java.isAssignableFrom(event))
        }
        assertFalse(TelemetryIntent::class.java.isAssignableFrom(TelemetryEvent::class.java))
    }

    @Test
    fun noIntentIsAnEvent() {
        intentSubclasses.forEach { intent ->
            assertTrue(TelemetryIntent::class.java.isAssignableFrom(intent))
            assertFalse("${intent.name} must not be an event", TelemetryEvent::class.java.isAssignableFrom(intent))
        }
        assertFalse(TelemetryEvent::class.java.isAssignableFrom(TelemetryIntent::class.java))
    }

    @Test
    fun nestedEventClasses_areListedCompletely() {
        fun nested(type: Class<*>): List<Class<*>> = type.declaredClasses.flatMap { listOf(it) + nested(it) }

        assertEquals(
            nested(TelemetryEvent::class.java).map { it.name }.sorted(),
            nestedEventClasses.map { it.name }.sorted()
        )
    }

    @Test
    fun noEventClass_declaresAThrowableField() {
        (listOf(TelemetryEvent::class.java) + nestedEventClasses).forEach { type ->
            type.declaredFields.forEach { field ->
                assertFalse(
                    "${type.name}.${field.name} must not hold a Throwable",
                    Throwable::class.java.isAssignableFrom(field.type)
                )
            }
        }
    }

    @Test
    fun networkFailureMethod_isAnHttpMethod() {
        assertEquals(
            TelemetryEvent.HttpMethod::class.java,
            TelemetryEvent.NetworkFailure::class.java.getDeclaredField("method").type
        )
    }
}
