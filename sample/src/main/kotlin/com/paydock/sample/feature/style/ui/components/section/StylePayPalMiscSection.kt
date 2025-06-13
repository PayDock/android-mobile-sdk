package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.feature.googlepay.presentation.GooglePayWidgetAppearance
import com.paydock.feature.paypal.checkout.presentation.PayPalWidgetAppearance
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.counter.NumberCounter
import com.paydock.sample.feature.style.ui.components.properties.googlepay.ButtonTypeDropdown
import com.paydock.sample.feature.style.ui.components.properties.shape.ShapeDropdown

@Composable
fun StylePayPalMiscSection(
    currentAppearance: PayPalWidgetAppearance,
    onAppearanceChange: (PayPalWidgetAppearance) -> Unit
) {
    // Derived states remain essential for UDF
    val currentButtonShape = currentAppearance.shape

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ShapeDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentShape = currentButtonShape,
            onShapeChange = { newShape ->
                onAppearanceChange(currentAppearance.copy(shape = newShape))
            },
        )
    }
}
