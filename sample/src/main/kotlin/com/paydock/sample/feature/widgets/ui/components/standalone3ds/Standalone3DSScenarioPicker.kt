package com.paydock.sample.feature.widgets.ui.components.standalone3ds

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paydock.sample.feature.threeDS.presentation.state.Standalone3DSScenario

private const val DISABLED_ALPHA = 0.5f
private const val CARD_SUFFIX_LENGTH = 4

/**
 * Initial page of the Widgets > Standalone 3DS demo: one button per GPayments TestLabs scenario.
 * Picking one vaults that scenario's test card and starts the flow.
 */
@Composable
internal fun Standalone3DSScenarioPicker(onSelect: (Standalone3DSScenario) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "TestLabs scenario",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Pick the flow to test. Each scenario uses its own GPayments TestLabs card.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Standalone3DSScenario.entries.forEach { scenario ->
            ScenarioButton(scenario = scenario, onClick = { onSelect(scenario) })
        }
    }
}

@Composable
private fun ScenarioButton(scenario: Standalone3DSScenario, onClick: () -> Unit) {
    val enabled = scenario.isConfigured
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else DISABLED_ALPHA),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = scenario.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (enabled) {
                    "Card ending ${scenario.cardNumber.takeLast(CARD_SUFFIX_LENGTH)}"
                } else {
                    "No test card set (Standalone3DSScenario.kt)"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
