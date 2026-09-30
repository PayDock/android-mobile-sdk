package com.paydock.sample.feature.widgets.ui.components.standalone3ds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paydock.sample.feature.threeDS.presentation.state.Standalone3DSDemoOutcome
import java.text.NumberFormat
import java.util.Currency

/**
 * Metadata of a finished standalone 3DS attempt: what the SDK reported plus what the demo measured.
 */
@Composable
internal fun Standalone3DSDetailsCard(
    outcome: Standalone3DSDemoOutcome,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            val rows = buildList {
                add("Status" to (outcome.status ?: "—"))
                outcome.description?.let { add("Description" to it) }
                add("Flow" to outcome.flowType.label)
                add("Amount" to formatAmount(outcome))
                outcome.errorMessage?.let { add("Error" to it) }
            }
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailRow(label = label, value = value)
            }
            outcome.charge3dsId?.let {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ChargeIdRow(chargeId = it)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ChargeIdRow(chargeId: String) {
    val clipboard = LocalClipboardManager.current
    Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
        Text(
            text = "Charge 3DS ID",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = chargeId,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
            IconButton(onClick = { clipboard.setText(AnnotatedString(chargeId)) }) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy charge 3DS ID",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatAmount(outcome: Standalone3DSDemoOutcome): String = runCatching {
    NumberFormat.getCurrencyInstance().apply { currency = Currency.getInstance(outcome.currency) }
        .format(outcome.amount)
}.getOrDefault("${outcome.amount} ${outcome.currency}")
