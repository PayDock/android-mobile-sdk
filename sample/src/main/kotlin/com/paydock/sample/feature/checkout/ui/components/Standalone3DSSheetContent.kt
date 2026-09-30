package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.presentation.Standalone3DSWidget
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppButtonVariant
import com.paydock.sample.feature.checkout.models.Standalone3DSFlowState
import com.paydock.sample.feature.checkout.models.Standalone3DSPhase
import kotlinx.coroutines.delay

private val CompactHeight = 320.dp
private const val CHALLENGE_HEIGHT_FRACTION = 0.8f
private const val STILL_WORKING_DELAY_MS = 8_000L

/**
 * The sheet phases are driven by `onProgress`, so the widget's own loader is replaced by this
 * no-op delegate to avoid a second overlay.
 */
private val PhaseDrivenLoadingDelegate = object : WidgetLoadingDelegate {
    override fun widgetLoadingDidStart() = Unit
    override fun widgetLoadingDidFinish() = Unit
}

/**
 * Standalone 3DS sheet content: keeps the widget (and its WebView/JS) running but hidden behind a
 * native status overlay, and only reveals it once the bank's challenge page has loaded.
 */
@Composable
fun Standalone3DSSheetContent(
    flow: Standalone3DSFlowState,
    threeDSToken: String?,
    onProgress: (Standalone3DSProgress) -> Unit,
    onResult: (Result<Standalone3DSResult>) -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    val phase = flow.phase
    val expanded = phase == Standalone3DSPhase.CHALLENGE_LOADING ||
        phase == Standalone3DSPhase.CHALLENGE ||
        (phase == Standalone3DSPhase.FINALIZING && flow.challengeShown)
    val challengeHeight = (LocalConfiguration.current.screenHeightDp * CHALLENGE_HEIGHT_FRACTION).dp
    val height by animateDpAsState(
        targetValue = if (expanded) challengeHeight else CompactHeight,
        label = "standalone3DSSheetHeight"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (!threeDSToken.isNullOrBlank()) {
            // A new token (retry) creates a fresh widget instance
            key(threeDSToken) {
                Standalone3DSWidget(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (phase == Standalone3DSPhase.CHALLENGE) 1f else 0f),
                    config = ThreeDSConfig(token = threeDSToken),
                    loadingDelegate = PhaseDrivenLoadingDelegate,
                    onProgress = onProgress,
                    completion = onResult
                )
            }
        }
        if (phase != Standalone3DSPhase.CHALLENGE) {
            Standalone3DSStatusOverlay(flow = flow, onRetry = onRetry, onClose = onClose)
        }
    }
}

@Composable
private fun Standalone3DSStatusOverlay(
    flow: Standalone3DSFlowState,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    val phase = flow.phase
    var stillWorking by remember(phase) { mutableStateOf(false) }
    if (phase == Standalone3DSPhase.VERIFYING) {
        LaunchedEffect(Unit) {
            delay(STILL_WORKING_DELAY_MS)
            stillWorking = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // Consume touches so the hidden WebView underneath can't be interacted with
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (phase) {
            Standalone3DSPhase.SUCCESS -> Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(56.dp)
            )

            Standalone3DSPhase.FAILED -> Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(56.dp)
            )

            else -> CircularProgressIndicator(modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = flow.title(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = flow.subtitle(stillWorking),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (phase == Standalone3DSPhase.FAILED) {
            Spacer(modifier = Modifier.height(24.dp))
            AppButton(
                text = "Try again",
                modifier = Modifier.fillMaxWidth(),
                onClick = onRetry
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                text = "Close",
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Outline,
                onClick = onClose
            )
        }
    }
}

private fun Standalone3DSFlowState.title(): String = when (phase) {
    Standalone3DSPhase.PREPARING -> "Preparing secure verification"
    Standalone3DSPhase.VERIFYING -> "Verifying your payment"
    Standalone3DSPhase.CHALLENGE_LOADING,
    Standalone3DSPhase.CHALLENGE -> "Your bank is asking for verification"

    Standalone3DSPhase.FINALIZING -> "Completing verification"
    Standalone3DSPhase.DECOUPLED -> "Approve the payment in your banking app"
    Standalone3DSPhase.SUCCESS -> "Payment verified"
    Standalone3DSPhase.FAILED -> if (declined) "Verification declined" else "Something went wrong"
}

private fun Standalone3DSFlowState.subtitle(stillWorking: Boolean): String = when (phase) {
    Standalone3DSPhase.PREPARING -> "Setting up 3-D Secure with your bank…"
    Standalone3DSPhase.VERIFYING ->
        if (stillWorking) "Still working — contacting your bank" else "This usually only takes a few seconds."

    Standalone3DSPhase.CHALLENGE_LOADING,
    Standalone3DSPhase.CHALLENGE -> "Loading the verification step…"

    Standalone3DSPhase.FINALIZING -> "Confirming the result with your bank…"
    Standalone3DSPhase.DECOUPLED -> decoupledDescription?.takeIf { it.isNotBlank() }
        ?: "Once you approve it there, this page will update automatically."

    Standalone3DSPhase.SUCCESS -> "Authentication completed successfully."
    Standalone3DSPhase.FAILED ->
        if (declined) {
            "Your bank declined the authentication for this payment."
        } else {
            "An unexpected error interrupted the authentication."
        }
}
