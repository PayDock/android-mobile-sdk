package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun GooglePayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    TabButton(
        isSelected = isSelected,
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_google_pay_default),
            contentDescription = null
        )
    }
}

@Composable
@PreviewLightDark
private fun GooglePayTabButtonDefault() {
    SampleTheme {
        GooglePayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@PreviewLightDark
private fun GooglePayTabButtonSelected() {
    SampleTheme {
        GooglePayTabButton(
            isSelected = true
        ) {}
    }
}