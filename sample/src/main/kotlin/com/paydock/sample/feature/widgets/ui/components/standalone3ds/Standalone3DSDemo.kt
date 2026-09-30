package com.paydock.sample.feature.widgets.ui.components.standalone3ds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.standalone.presentation.Standalone3DSWidget
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppButtonVariant
import com.paydock.sample.feature.checkout.models.Standalone3DSFlowState
import com.paydock.sample.feature.checkout.models.Standalone3DSPhase
import com.paydock.sample.feature.threeDS.presentation.Standalone3DSDemoViewModel
import com.paydock.sample.feature.threeDS.presentation.state.Standalone3DSDemoOutcome
import com.paydock.sample.feature.threeDS.presentation.state.Standalone3DSDemoUIState
import kotlinx.coroutines.delay

private const val STILL_WORKING_DELAY_MS = 8_000L
private const val TICK_MS = 1_000L
private const val DETAILS_ENTER_DELAY_MS = 750
private const val DETAILS_ENTER_MS = 300

/**
 * The phases drive the UI, so the widget's own loader is replaced by this no-op delegate.
 */
private val PhaseDrivenLoadingDelegate = object : WidgetLoadingDelegate {
    override fun widgetLoadingDidStart() = Unit
    override fun widgetLoadingDidFinish() = Unit
}

/**
 * Widgets > Standalone 3DS playground. The widget (and its WebView) runs at full size but invisible
 * behind a native status panel, and is only revealed once the bank's challenge page has loaded — the
 * same presentation as the web playground. On completion the widget is removed and the outcome shown.
 */
@Composable
fun Standalone3DSDemo(
    viewModel: Standalone3DSDemoViewModel,
    onDone: () -> Unit,
) {
    val state by viewModel.stateFlow.collectAsState()
    val phase = state.flow.phase

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Secure verification · 3-D Secure",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            state.threeDSToken?.takeIf { it.isNotBlank() }?.let { token ->
                // A new attempt creates a fresh widget instance
                key(state.attempt, token) {
                    val visible = phase == Standalone3DSPhase.CHALLENGE
                    Standalone3DSWidget(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if (visible) 1f else 0f)
                            .then(if (visible) Modifier else Modifier.semantics { invisibleToUser() }),
                        config = ThreeDSConfig(token = token),
                        loadingDelegate = PhaseDrivenLoadingDelegate,
                        onProgress = viewModel::handleProgress,
                        completion = viewModel::handleResult
                    )
                }
            }
            if (phase != Standalone3DSPhase.CHALLENGE) {
                StatusPanel(
                    state = state,
                    onRestart = viewModel::start,
                    onDone = onDone
                )
            }
        }
    }
}

@Composable
private fun StatusPanel(
    state: Standalone3DSDemoUIState,
    onRestart: () -> Unit,
    onDone: () -> Unit,
) {
    val flow = state.flow
    val phase = flow.phase
    val terminal = phase == Standalone3DSPhase.SUCCESS || phase == Standalone3DSPhase.FAILED
    val stillWorking = rememberStillWorking(phase)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            // Consume touches so the invisible WebView underneath can't be interacted with
            .pointerInput(Unit) { detectTapGestures { } }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = if (terminal) Arrangement.Top else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (terminal) Spacer(modifier = Modifier.height(24.dp))
        PhaseIcon(phase)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = flow.title(stillWorking),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = flow.subtitle(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (terminal) {
            state.outcome?.let { OutcomeDetails(outcome = it, phase = phase) }
            TerminalActions(success = phase == Standalone3DSPhase.SUCCESS, onRestart = onRestart, onDone = onDone)
        } else {
            ElapsedTicker(key = state.attempt, running = phase != Standalone3DSPhase.PREPARING)
        }
    }
}

@Composable
private fun PhaseIcon(phase: Standalone3DSPhase) {
    when (phase) {
        Standalone3DSPhase.SUCCESS -> Standalone3DSSuccessMark()
        Standalone3DSPhase.FAILED -> Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(72.dp)
        )

        else -> CircularProgressIndicator(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun OutcomeDetails(outcome: Standalone3DSDemoOutcome, phase: Standalone3DSPhase) {
    val reduceMotion = rememberReduceMotion()
    // On success the details rise in after the checkmark has drawn
    val animate = phase == Standalone3DSPhase.SUCCESS && !reduceMotion
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(!animate) }
            .apply { targetState = true },
        enter = fadeIn(tween(DETAILS_ENTER_MS, delayMillis = DETAILS_ENTER_DELAY_MS)) +
            slideInVertically(tween(DETAILS_ENTER_MS, delayMillis = DETAILS_ENTER_DELAY_MS)) { it / 8 }
    ) {
        Standalone3DSDetailsCard(outcome = outcome, modifier = Modifier.padding(top = 24.dp))
    }
}

@Composable
private fun TerminalActions(success: Boolean, onRestart: () -> Unit, onDone: () -> Unit) {
    Spacer(modifier = Modifier.height(24.dp))
    AppButton(
        text = if (success) "Run again" else "Try again",
        modifier = Modifier.fillMaxWidth(),
        onClick = onRestart
    )
    Spacer(modifier = Modifier.height(8.dp))
    AppButton(
        text = if (success) "Done" else "Close",
        modifier = Modifier.fillMaxWidth(),
        variant = AppButtonVariant.Outline,
        onClick = onDone
    )
}

@Composable
private fun ElapsedTicker(key: Int, running: Boolean) {
    var seconds by remember(key) { mutableIntStateOf(0) }
    if (running) {
        LaunchedEffect(key) {
            while (true) {
                delay(TICK_MS)
                seconds++
            }
        }
    }
    if (seconds > 0) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$seconds s",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun rememberStillWorking(phase: Standalone3DSPhase): Boolean {
    var stillWorking by remember(phase) { mutableStateOf(false) }
    if (phase == Standalone3DSPhase.VERIFYING) {
        LaunchedEffect(Unit) {
            delay(STILL_WORKING_DELAY_MS)
            stillWorking = true
        }
    }
    return stillWorking
}

// Copy matches the web playground (client-sdk canvas-3ds standalone3ds-modal) and the checkout sheet
private fun Standalone3DSFlowState.title(stillWorking: Boolean): String = when (phase) {
    Standalone3DSPhase.PREPARING -> "Preparing secure verification"
    Standalone3DSPhase.VERIFYING -> if (stillWorking) "Still working — contacting your bank" else "Verifying your payment"
    Standalone3DSPhase.CHALLENGE_LOADING,
    Standalone3DSPhase.CHALLENGE -> "Your bank is asking for verification"

    Standalone3DSPhase.FINALIZING -> "Completing verification"
    Standalone3DSPhase.DECOUPLED -> "Approve the payment in your banking app"
    Standalone3DSPhase.SUCCESS -> "Payment verified"
    Standalone3DSPhase.FAILED -> if (declined) "Verification declined" else "Something went wrong"
}

private fun Standalone3DSFlowState.subtitle(): String = when (phase) {
    Standalone3DSPhase.PREPARING -> "Setting up 3-D Secure with your bank…"
    Standalone3DSPhase.VERIFYING -> "This usually only takes a few seconds."
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
