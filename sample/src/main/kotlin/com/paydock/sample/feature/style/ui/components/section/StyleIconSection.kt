package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.icon.IconAppearance
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.color.ColorPickerField
import com.paydock.sample.feature.style.ui.components.core.counter.NumberCounter

@Composable
fun StyleIconSection(
    currentAppearance: IconAppearance,
    onAppearanceChange: (IconAppearance) -> Unit
) {
    // Derived states remain essential for UDF
    val currentTintColor = currentAppearance.tint
    val currentSize = currentAppearance.size

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ColorPickerField(
            label = stringResource(R.string.label_tint_colour),
            currentColor = currentTintColor,
            onColorChange = { newColor ->
                onAppearanceChange(
                    currentAppearance.copy(tint = newColor)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        NumberCounter(
            title = stringResource(R.string.label_size),
            value = currentSize.value.toInt(),
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(size = newValue.dp))
            }
        )
    }
}