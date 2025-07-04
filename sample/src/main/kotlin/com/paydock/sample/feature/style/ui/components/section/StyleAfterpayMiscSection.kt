package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.afterpay.presentation.AfterpayWidgetAppearance
import com.paydock.sample.feature.style.ui.components.properties.afterpay.ButtonTextDropdown
import com.paydock.sample.feature.style.ui.components.properties.afterpay.ColorSchemeDropdown

@Composable
fun StyleAfterpayMiscSection(
    currentAppearance: AfterpayWidgetAppearance,
    onAppearanceChange: (AfterpayWidgetAppearance) -> Unit
) {
    // Derived states remain essential for UDF
    val currentButtonText = currentAppearance.buttonText
    val currentColorScheme = currentAppearance.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ButtonTextDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentButtonText = currentButtonText,
            onAfterpayButtonTextChange = { newButtonText ->
                onAppearanceChange(
                    currentAppearance.copy(buttonText = newButtonText)
                )
            },
        )

        HorizontalDivider()

        ColorSchemeDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentColorScheme = currentColorScheme,
            onAfterpayColorSchemeChange = { newColorScheme ->
                onAppearanceChange(
                    currentAppearance.copy(colorScheme = newColorScheme)
                )
            },
        )
    }
}
