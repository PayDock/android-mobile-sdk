package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun AfterpayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    TabButton(
        isSelected = isSelected,
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_afterpay),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
@PreviewLightDark
private fun AfterpayTabButtonDefault() {
    SampleTheme {
        AfterpayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@PreviewLightDark
private fun AfterpayTabButtonSelected() {
    SampleTheme {
        AfterpayTabButton(
            isSelected = true
        ) {}
    }
}