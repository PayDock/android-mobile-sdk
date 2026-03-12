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
import com.paydock.designsystems.components.button.ImageButtonAppearance
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.FloatCounter
import com.paydock.sample.feature.style.ui.components.core.color.ColorPickerField
import com.paydock.sample.feature.style.ui.components.properties.shape.CornerUnit
import com.paydock.sample.feature.style.ui.components.properties.shape.ShapeDropdown

@Composable
fun StyleImageButtonSection(
    currentAppearance: ImageButtonAppearance,
    onAppearanceChange: (ImageButtonAppearance) -> Unit
) {
    // Derived states remain essential for UDF
    val currentRippleColor = currentAppearance.rippleColor
    val currentShape = currentAppearance.shape
    val currentDisabledAlpha = currentAppearance.disabledImageAlpha

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ColorPickerField(
            label = stringResource(R.string.label_ripple_colour),
            currentColor = currentRippleColor,
            onColorChange = { newColor ->
                onAppearanceChange(
                    currentAppearance.copy(rippleColor = newColor)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        FloatCounter(
            title = stringResource(R.string.label_disabled_alpha),
            value = currentDisabledAlpha,
            onValueChange = { newValue ->
                onAppearanceChange(
                    currentAppearance.copy(disabledImageAlpha = newValue)
                )
            }
        )

        HorizontalDivider()

        ShapeDropdown(
            modifier = Modifier.fillMaxWidth(),
            containerLabel = stringResource(R.string.label_shape_clipping),
            defaultUnit = CornerUnit.PERCENTAGE,
            currentShape = currentShape,
            onShapeChange = { newShape ->
                onAppearanceChange(currentAppearance.copy(shape = newShape))
            },
        )
    }
}