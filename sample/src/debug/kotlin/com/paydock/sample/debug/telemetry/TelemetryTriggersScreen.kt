package com.paydock.sample.debug.telemetry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.paydock.MobileSDK
import com.paydock.core.domain.error.exceptions.PayPalDataCollectorException
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.feature.paypal.fraud.domain.model.integration.PayPalDataCollectorConfig
import com.paydock.feature.paypal.fraud.utils.PayPalDataCollectorUtil
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.standalone.presentation.Standalone3DSWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.designsystems.components.button.AppButton
import kotlinx.coroutines.launch

private const val ANR_BLOCK_MS = 120_000L

// Blank, not malformed: sandbox answers a blank access token with 403 but a malformed one ("invalid") with 500, so
// the HTTP 4xx trigger needs it blank. A blank 3DS token is still an InvalidTokenException.
private const val INVALID_TOKEN = ""

/** A fixed, obviously fake gateway ID: the call fails on the invalid token, so no real gateway is needed. */
private const val TEST_GATEWAY_ID = "telemetry-test-gateway"
private const val CONNECTIONS_RESET = "connection reset"
private const val CONNECTIONS_NOT_RESET = "couldn't reset connections — tap right after launching"
private const val RUNNING = "running…"
private const val SUCCESS = "success"
private const val OPENED = "opened"

/** The trigger buttons, in screen order (R_08). Each one action; each outcome shows as a type name only. */
private enum class Trigger(val label: String) {
    CRASH_IN_SDK("Crash in SDK"),
    ANR("ANR"),
    SDK_ERROR("SDK error"),
    WIDGET_OPEN("Widget open"),
    HTTP_4XX("HTTP 4xx"),
    TIMEOUT("Timeout"),
    NO_CONNECTION("No connection"),
}

/**
 * Debug-only: one button per telemetry record type, for a manual devdock run (README "Telemetry", "The trigger screen").
 * Shows each outcome as the exception's simple name, never a message, a token or an ID.
 */
@Composable
fun TelemetryTriggersScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val outcomes = remember { mutableStateMapOf<Trigger, String>() }
    var openWidget by remember { mutableStateOf<Trigger?>(null) }

    fun runApiCall(trigger: Trigger, mode: StandInProxy.Mode?) {
        outcomes[trigger] = RUNNING
        scope.launch {
            outcomes[trigger] = if (mode == null) {
                walletConfigCall()
            } else {
                StandInProxy().around(mode) { connectionsReset ->
                    val note = if (connectionsReset) CONNECTIONS_RESET else CONNECTIONS_NOT_RESET
                    "${walletConfigCall()} ($note)"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("telemetry_triggers_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Each button produces one telemetry record type. Crash and ANR records arrive on the next " +
                "launch. Timeout takes about 60 s.",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "ANR: tap the screen once more, wait for the 'isn't responding' dialog, then tap Close app. " +
                "Waiting it out records nothing.",
            style = MaterialTheme.typography.bodySmall
        )
        Trigger.entries.forEach { trigger ->
            AppButton(
                text = trigger.label,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("telemetry_trigger_${trigger.name.lowercase()}"),
                enabled = outcomes[trigger] != RUNNING,
                onClick = {
                    when (trigger) {
                        // The SDK itself throws (a second initialisation), uncaught on the main thread.
                        Trigger.CRASH_IN_SDK -> MobileSDK.Builder()
                            .environment(BuildConfig.SDK_ENVIRONMENT)
                            .build(context.applicationContext)

                        Trigger.ANR -> {
                            Thread.sleep(ANR_BLOCK_MS)
                            outcomes[trigger] = SUCCESS
                        }

                        Trigger.SDK_ERROR, Trigger.WIDGET_OPEN -> {
                            outcomes[trigger] = OPENED
                            openWidget = trigger
                        }

                        Trigger.HTTP_4XX -> runApiCall(trigger, mode = null)
                        Trigger.TIMEOUT -> runApiCall(trigger, StandInProxy.Mode.HANG)
                        Trigger.NO_CONNECTION -> runApiCall(trigger, StandInProxy.Mode.REFUSE)
                    }
                }
            )
            outcomes[trigger]?.let { outcome ->
                Text(
                    text = "${trigger.label}: $outcome",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("telemetry_outcome_${trigger.name.lowercase()}")
                )
            }
        }
        when (openWidget) {
            Trigger.SDK_ERROR -> Standalone3DSWidget(
                config = ThreeDSConfig(token = INVALID_TOKEN),
                completion = { result -> outcomes[Trigger.SDK_ERROR] = result.outcomeName() }
            )

            Trigger.WIDGET_OPEN -> CardDetailsWidget(
                config = CardDetailsWidgetConfig(accessToken = BuildConfig.ACCESS_TOKEN_WIDGET),
                completion = { result -> outcomes[Trigger.WIDGET_OPEN] = result.outcomeName() }
            )

            else -> Unit
        }
    }
}

/** GET /v1/gateways/{id}/wallet-config with a blank access token and a fake test gateway ID. */
private suspend fun walletConfigCall(): String = try {
    PayPalDataCollectorUtil.initialise(
        PayPalDataCollectorConfig(accessToken = INVALID_TOKEN, gatewayId = TEST_GATEWAY_ID)
    )
    SUCCESS
} catch (e: PayPalDataCollectorException) {
    e.outcomeName()
}

private fun Throwable.outcomeName(): String = this::class.java.simpleName

private fun Result<*>.outcomeName(): String = exceptionOrNull()?.outcomeName() ?: SUCCESS
