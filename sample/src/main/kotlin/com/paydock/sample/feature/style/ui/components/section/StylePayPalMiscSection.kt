package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.paypal.checkout.presentation.PayPalWidgetAppearance
import com.paydock.sample.feature.style.ui.components.properties.paypal.PayPalButtonColorDropdown
import com.paydock.sample.feature.style.ui.components.properties.paypal.PayPalButtonLabelDropdown
import com.paydock.sample.feature.style.ui.components.properties.paypal.PayPalButtonShapeDropdown

@Composable
fun StylePayPalMiscSection(
    currentAppearance: PayPalWidgetAppearance,
    onAppearanceChange: (PayPalWidgetAppearance) -> Unit
) {
    val currentColor = currentAppearance.buttonColour
    val currentLabel = currentAppearance.buttonLabel
    val currentShape = currentAppearance.buttonShape

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        PayPalButtonColorDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentColor = currentColor,
            onPayPalColorChange = { newColor ->
                onAppearanceChange(currentAppearance.copy(paypalColor = newColor))
            }
        )

        HorizontalDivider()

        PayPalButtonLabelDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentLabel = currentLabel,
            onPayPalLabelChange = { newLabel ->
                onAppearanceChange(currentAppearance.copy(paypalLabel = newLabel))
            }
        )

        HorizontalDivider()

        PayPalButtonShapeDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentShape = currentShape,
            onPayPalShapeChange = { newShape ->
                onAppearanceChange(currentAppearance.copy(buttonShape = newShape))
            }
        )
    }
}


