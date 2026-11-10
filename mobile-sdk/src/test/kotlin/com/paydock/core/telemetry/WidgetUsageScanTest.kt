package com.paydock.core.telemetry

import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * Source scan for widget usage (R_06): each of the 12 public widget composables holds exactly one
 * `ReportWidgetOpened(SdkWidget.<X>)` call, naming its own composable, as the first statement of its body (so before
 * any early return), and [SdkWidget] has exactly these 12 names.
 */
internal class WidgetUsageScanTest {

    /** The source file of each public widget, relative to `src/main/kotlin/com/paydock/`, by its [SdkWidget] name. */
    private val widgetFiles: Map<String, String> = mapOf(
        "CARD_DETAILS" to "feature/card/presentation/CardDetailsWidget.kt",
        "GIFT_CARD" to "feature/card/presentation/GiftCardWidget.kt",
        "ADDRESS_DETAILS" to "feature/address/presentation/AddressDetailsWidget.kt",
        "AFTERPAY" to "feature/afterpay/presentation/AfterpayWidget.kt",
        "COLES_PAY" to "feature/colespay/presentation/ColesPayWidget.kt",
        "GOOGLE_PAY" to "feature/googlepay/presentation/GooglePayWidget.kt",
        "PAYPAL" to "feature/paypal/checkout/presentation/PayPalWidget.kt",
        "PAYPAL_SAVE_PAYMENT_SOURCE" to "feature/paypal/vault/presentation/PayPalSavePaymentSourceWidget.kt",
        "CLICK_TO_PAY" to "feature/src/presentation/ClickToPayWidget.kt",
        "MPGS_3DS" to "feature/threeDS/integrated/presentation/MPGS3dsWidget.kt",
        "STANDALONE_3DS" to "feature/threeDS/standalone/presentation/Standalone3DSWidget.kt",
        "ZIP" to "feature/zip/presentation/ZipWidget.kt",
    )

    private val usageCall = Regex("""ReportWidgetOpened\(SdkWidget\.(\w+)\)""")

    /** Every `const val` of [SdkWidget], by its name. */
    private fun sdkWidgetNames(): Map<String, String> = SdkWidget::class.java.declaredFields
        .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
        .associate { it.name to it.get(null) as String }

    @Test
    fun sdkWidget_hasExactlyTheTwelveWidgetNames() {
        val expected = mapOf(
            "CARD_DETAILS" to "CardDetailsWidget",
            "GIFT_CARD" to "GiftCardWidget",
            "ADDRESS_DETAILS" to "AddressDetailsWidget",
            "AFTERPAY" to "AfterpayWidget",
            "COLES_PAY" to "ColesPayWidget",
            "GOOGLE_PAY" to "GooglePayWidget",
            "PAYPAL" to "PayPalWidget",
            "PAYPAL_SAVE_PAYMENT_SOURCE" to "PayPalSavePaymentSourceWidget",
            "CLICK_TO_PAY" to "ClickToPayWidget",
            "MPGS_3DS" to "MPGS3dsWidget",
            "STANDALONE_3DS" to "Standalone3DSWidget",
            "ZIP" to "ZipWidget",
        )
        assertEquals(expected, sdkWidgetNames())
        assertEquals(sdkWidgetNames().keys, widgetFiles.keys)
    }

    @Test
    fun eachWidgetFile_holdsExactlyOneUsageCall_namingItsOwnComposable() {
        val names = sdkWidgetNames()
        widgetFiles.forEach { (constant, file) ->
            val source = WidgetSourceScan.read(file)
            assertEquals("$file: ReportWidgetOpened calls", 1, WidgetSourceScan.count(source, "ReportWidgetOpened("))
            val calls = usageCall.findAll(source).map { it.groupValues[1] }.toList()
            assertEquals("$file: usage call", listOf(constant), calls)
            val composable = names.getValue(constant)
            val body = WidgetSourceScan.function(source, composable)
            assertEquals("$file: usage call inside fun $composable", 1, usageCall.findAll(body).count())
        }
    }

    @Test
    fun eachUsageCall_isTheFirstStatementOfItsComposable() {
        val names = sdkWidgetNames()
        widgetFiles.forEach { (constant, file) ->
            val composable = names.getValue(constant)
            val function = WidgetSourceScan.function(WidgetSourceScan.read(file), composable)
            val bodyStart = function.indexOf("\n) {\n")
            assertEquals("$file: fun $composable has a body", true, bodyStart >= 0)
            val firstStatement = function.substring(bodyStart + "\n) {\n".length)
                .lineSequence()
                .map { it.trim() }
                .first { it.isNotEmpty() && !it.startsWith("//") }
            assertEquals("$file: first statement", "ReportWidgetOpened(SdkWidget.$constant)", firstStatement)
        }
    }
}
