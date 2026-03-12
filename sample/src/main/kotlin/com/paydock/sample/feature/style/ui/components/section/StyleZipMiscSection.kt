package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.zip.presentation.ZipWidgetAppearance
import com.paydock.sample.feature.style.ui.components.properties.zip.ButtonStyleDropdown

@Composable
fun StyleZipMiscSection(
    currentAppearance: ZipWidgetAppearance,
    onAppearanceChange: (ZipWidgetAppearance) -> Unit
) {
    val currentButtonStyle = currentAppearance.buttonStyle

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ButtonStyleDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentButtonStyle = currentButtonStyle,
            onButtonStyleChange = { newButtonStyle ->
                onAppearanceChange(
                    currentAppearance.copy(buttonStyle = newButtonStyle)
                )
            },
        )
    }
}

