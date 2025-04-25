package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
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
            painter = painterResource(id = R.drawable.ic_src),
            contentDescription = null,
            tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.label_click_to_pay),
            color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
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