package com.paydock.core.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guard scans for the SDK's error reporting (R_09):
 * - errors are reported only at their failure paths, so no `CoroutineExceptionHandler` and no
 *   `TelemetryEvent.UncaughtException` appear in the SDK's main sources;
 * - the forwarding view models (`BaseViewModel`, `WalletViewModel`) stay untouched: they neither report nor hold the
 *   facade;
 * - each view model's hand-written `reportWidgetError` reports under its own widget's name.
 *
 * The working directory of a module's unit tests is the module directory.
 */
internal class TelemetryGuardTest {

    private val mainRoot = File("src/main")

    private val forwardingViewModels = listOf(
        "src/main/kotlin/com/paydock/core/presentation/viewmodels/BaseViewModel.kt",
        "src/main/kotlin/com/paydock/feature/wallet/presentation/viewmodels/WalletViewModel.kt",
    )

    private val coroutineExceptionHandler = Regex("""\bCoroutineExceptionHandler\b""")
    private val uncaughtExceptionEvent = Regex("""\bUncaughtException\b""")

    private fun mainSources(): List<File> = mainRoot.walkTopDown()
        .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
        .toList()

    @Test
    fun mainSources_exist() {
        assertTrue("no sources found under ${mainRoot.absolutePath}", mainSources().isNotEmpty())
    }

    @Test
    fun mainSources_haveNoCoroutineExceptionHandler() {
        mainSources().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                assertFalse(
                    "${file.path}:${index + 1} must not use a CoroutineExceptionHandler: $line",
                    coroutineExceptionHandler.containsMatchIn(line)
                )
            }
        }
    }

    @Test
    fun mainSources_doNotReportUncaughtExceptions() {
        mainSources().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                assertFalse(
                    "${file.path}:${index + 1} must not refer to TelemetryEvent.UncaughtException: $line",
                    uncaughtExceptionEvent.containsMatchIn(line)
                )
            }
        }
    }

    /** Each view model with a `reportWidgetError`, and the one widget name it must report under. */
    private val widgetErrorReporters = mapOf(
        "feature/afterpay/presentation/viewmodels/AfterpayViewModel.kt" to "AFTERPAY",
        "feature/colespay/presentation/viewmodels/ColesPayViewModel.kt" to "COLES_PAY",
        "feature/googlepay/presentation/viewmodels/GooglePayViewModel.kt" to "GOOGLE_PAY",
        "feature/paypal/checkout/presentation/viewmodel/PayPalViewModel.kt" to "PAYPAL",
        "feature/paypal/vault/presentation/viewmodel/PayPalVaultViewModel.kt" to "PAYPAL_SAVE_PAYMENT_SOURCE",
        "feature/src/presentation/viewmodels/ClickToPayViewModel.kt" to "CLICK_TO_PAY",
        "feature/threeDS/integrated/presentation/viewmodels/MPGS3dsViewModel.kt" to "MPGS_3DS",
        "feature/threeDS/standalone/presentation/viewmodels/Standalone3DSViewModel.kt" to "STANDALONE_3DS",
    )

    private val reportWidgetErrorDeclaration = "fun reportWidgetError("
    private val sdkWidgetName = Regex("""SdkWidget\.([A-Z0-9_]+)""")

    @Test
    fun everyReportWidgetError_isListed() {
        val declaring = File("src/main/kotlin/com/paydock").walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.readText().contains(reportWidgetErrorDeclaration) }
            .map { it.invariantSeparatorsPath.removePrefix("src/main/kotlin/com/paydock/") }
            .toSet()

        assertEquals(widgetErrorReporters.keys, declaring)
    }

    @Test
    fun eachReportWidgetError_namesItsOwnWidget() {
        widgetErrorReporters.forEach { (path, widget) ->
            val source = File("src/main/kotlin/com/paydock/$path").readText()
            val start = source.indexOf(reportWidgetErrorDeclaration)
            assertTrue("$path declares no reportWidgetError", start >= 0)
            val end = source.indexOf("\n    }\n", start)
            assertTrue("$path: no end for reportWidgetError", end >= 0)
            val names = sdkWidgetName.findAll(source.substring(start, end)).map { it.groupValues[1] }.toList()
            assertEquals("$path: reportWidgetError must report under its own widget", listOf(widget), names)
        }
    }

    @Test
    fun forwardingViewModels_doNotReportOrHoldTheFacade() {
        forwardingViewModels.forEach { path ->
            val source = File(path).readText()
            assertFalse("$path must not call reportSdkError", source.contains("reportSdkError"))
            assertFalse("$path must not refer to Telemetry", source.contains("Telemetry"))
        }
    }
}
