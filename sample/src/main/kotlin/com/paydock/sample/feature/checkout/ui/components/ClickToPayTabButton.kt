package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun ClickToPayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    val selectedColor = MaterialTheme.colorScheme.primary
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = selectedColor,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_clicktopay),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }
}

@Composable
@PreviewLightDark
private fun CardTabButtonDefault() {
    SampleTheme {
        ClickToPayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@PreviewLightDark
private fun CardTabButtonSelected() {
    SampleTheme {
        ClickToPayTabButton(
            isSelected = true
        ) {}
    }
}