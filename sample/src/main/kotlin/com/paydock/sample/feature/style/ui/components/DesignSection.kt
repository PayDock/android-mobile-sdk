package com.paydock.sample.feature.style.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.ThemeDimensions
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.components.fields.NumberCounter

@Composable
fun DesignSection(
    dimensionsTheme: ThemeDimensions,
    onDimensionsUpdated: (ThemeDimensions) -> Unit,
) {
    SectionContainer(title = stringResource(R.string.label_design)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_text_corner_radius),
                value = dimensionsTheme.textFieldCornerRadius.value.toInt(),
                onValueChange = {
                    onDimensionsUpdated(dimensionsTheme.copy(textFieldCornerRadius = it.dp))
                })
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_button_corner_radius),
                value = dimensionsTheme.buttonCornerRadius.value.toInt(),
                onValueChange = {
                    onDimensionsUpdated(dimensionsTheme.copy(buttonCornerRadius = it.dp))
                })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_shadow),
                value = dimensionsTheme.shadow.value.toInt(),
                onValueChange = {
                    onDimensionsUpdated(dimensionsTheme.copy(shadow = it.dp))
                })
            Spacer(modifier = Modifier.weight(0.5f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_border_width),
                value = dimensionsTheme.borderWidth.value.toInt(),
                onValueChange = {
                    onDimensionsUpdated(dimensionsTheme.copy(borderWidth = it.dp))
                })
            NumberCounter(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.label_spacing),
                value = dimensionsTheme.spacing.value.toInt(),
                onValueChange = {
                    onDimensionsUpdated(dimensionsTheme.copy(spacing = it.dp))
                })
        }
    }
}