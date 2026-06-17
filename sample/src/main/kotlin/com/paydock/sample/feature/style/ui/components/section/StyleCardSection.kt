package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.card.CardAppearance
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.NumberCounter
import com.paydock.sample.feature.style.ui.components.core.color.ColorPickerField

@Composable
fun StyleCardSection(
    currentAppearance: CardAppearance,
    onAppearanceChange: (CardAppearance) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ColorPickerField(
            label = stringResource(R.string.label_container_colour),
            currentColor = currentAppearance.cardColors.containerColor,
            onColorChange = { newColor ->
                onAppearanceChange(
                    currentAppearance.copy(
                        cardColors = currentAppearance.cardColors.copy(containerColor = newColor)
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        NumberCounter(
            title = stringResource(R.string.label_corner_radius_dp),
            value = currentAppearance.cornerRadius.value.toInt(),
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(cornerRadius = newValue.dp))
            }
        )

        NumberCounter(
            title = "Internal Padding", // Using a literal or you could add a string resource
            value = (currentAppearance.contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr).value).toInt(),
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(contentPadding = PaddingValues(newValue.dp)))
            }
        )
    }
}
