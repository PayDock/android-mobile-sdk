package com.paydock.core.telemetry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.paydock.telemetry.Telemetry

/**
 * The one usage line of a public widget (R_06): looks up the Telemetry facade in the SDK's global Koin without
 * failing (none in previews or tests → nothing), remembered across recompositions, then
 * LaunchedEffect(widget) { telemetry?.reportWidgetOpened(widget) }.
 *
 * @param widget The [SdkWidget] name of the calling widget.
 */
@Composable
internal fun ReportWidgetOpened(widget: String) {
    val telemetry: Telemetry? = remember { telemetryOrNull() }
    LaunchedEffect(widget) { telemetry?.reportWidgetOpened(widget) }
}

/**
 * Pattern D for a composition-time path before the widget has its view model (MP2, SD3): looks the facade up with
 * telemetryOrNull(), remembered, then LaunchedEffect(error::class) { telemetry?.reportSdkError(error, widget) }.
 * Replaces the two private ReportInvalidToken copies; lives in ReportWidgetOpened.kt.
 *
 * @param error The error the merchant gets.
 * @param widget The [SdkWidget] name of the calling widget.
 */
@Composable
internal fun ReportSdkErrorOnce(error: Throwable, widget: String) {
    val telemetry: Telemetry? = remember { telemetryOrNull() }
    LaunchedEffect(error::class) { telemetry?.reportSdkError(error, widget) }
}
