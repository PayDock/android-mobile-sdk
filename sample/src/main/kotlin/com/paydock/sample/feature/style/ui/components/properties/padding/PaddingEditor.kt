package com.paydock.sample.feature.style.ui.components.properties.padding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.components.fields.NumberCounter

enum class PaddingSide {
    TOP, BOTTOM, START, END
}

@Composable
fun PaddingEditor(
    modifier: Modifier = Modifier,
    title: String,
    currentPaddingValues: PaddingValues,
    onPaddingChange: (PaddingValues) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    var applyPaddingToAllSides by remember { mutableStateOf(false) }

    // This internal helper function now calls the onPaddingChange callback
    fun updateLocalPadding(changedValue: Dp, changedSide: PaddingSide) {
        val newPaddingValues = if (applyPaddingToAllSides) {
            PaddingValues(all = changedValue.coerceAtLeast(0.dp))
        } else {
            val top =
                if (changedSide == PaddingSide.TOP) changedValue.coerceAtLeast(0.dp) else currentPaddingValues.calculateTopPadding()
            val bottom =
                if (changedSide == PaddingSide.BOTTOM) changedValue.coerceAtLeast(0.dp) else currentPaddingValues.calculateBottomPadding()
            val start =
                if (changedSide == PaddingSide.START) changedValue.coerceAtLeast(0.dp) else currentPaddingValues.calculateStartPadding(
                    layoutDirection
                )
            val end =
                if (changedSide == PaddingSide.END) changedValue.coerceAtLeast(0.dp) else currentPaddingValues.calculateEndPadding(
                    layoutDirection
                )
            PaddingValues(top = top, bottom = bottom, start = start, end = end)
        }
        onPaddingChange(newPaddingValues)
    }

    SectionContainer(
        modifier = modifier.fillMaxWidth(),
        title = title
    ) {
        val topPaddingValue = currentPaddingValues.calculateTopPadding().value.toInt()
        val bottomPaddingValue = currentPaddingValues.calculateBottomPadding().value.toInt()
        val startPaddingValue =
            currentPaddingValues.calculateStartPadding(layoutDirection).value.toInt()
        val endPaddingValue =
            currentPaddingValues.calculateEndPadding(layoutDirection).value.toInt()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { applyPaddingToAllSides = !applyPaddingToAllSides }
        ) {
            Checkbox(
                checked = applyPaddingToAllSides,
                onCheckedChange = { applyPaddingToAllSides = it }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.label_apply_all_sides), // Make sure R.string.label_apply_all_sides exists
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_top), // Make sure R.string.label_top exists
                value = topPaddingValue,
                onValueChange = { newValue -> updateLocalPadding(newValue.dp, PaddingSide.TOP) }
            )
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_bottom), // Make sure R.string.label_bottom exists
                value = bottomPaddingValue,
                onValueChange = { newValue -> updateLocalPadding(newValue.dp, PaddingSide.BOTTOM) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_start), // Make sure R.string.label_start exists
                value = startPaddingValue,
                onValueChange = { newValue -> updateLocalPadding(newValue.dp, PaddingSide.START) }
            )
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_end), // Make sure R.string.label_end exists
                value = endPaddingValue,
                onValueChange = { newValue -> updateLocalPadding(newValue.dp, PaddingSide.END) }
            )
        }
    }
}