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
import com.paydock.feature.address.presentation.AddressDetailsWidgetAppearance
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.counter.NumberCounter

@Composable
fun StyleAddressDetailsMiscSection(
    currentAppearance: AddressDetailsWidgetAppearance,
    onAppearanceChange: (AddressDetailsWidgetAppearance) -> Unit
) {
    // Derived states remain essential for UDF
    val currentVerticalSpacing = currentAppearance.verticalSpacing
    val currentHorizontalSpacing = currentAppearance.horizontalSpacing

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        NumberCounter(
            title = stringResource(R.string.label_vertical_spacing),
            value = currentVerticalSpacing.value.toInt(),
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(verticalSpacing = newValue.dp))
            }
        )

        HorizontalDivider()

        NumberCounter(
            title = stringResource(R.string.label_horizontal_spacing),
            value = currentHorizontalSpacing.value.toInt(),
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(horizontalSpacing = newValue.dp))
            }
        )
    }
}
