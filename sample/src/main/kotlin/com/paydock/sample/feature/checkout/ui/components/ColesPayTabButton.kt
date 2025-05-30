package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun ColesPayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    TabButton(
        isSelected = isSelected,
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_coles_pay),
            contentDescription = null
        )
    }
}

@Composable
@Preview
private fun ColesPayTabButtonDefault() {
    SampleTheme {
        ColesPayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@Preview
private fun ColesPayTabButtonSelected() {
    SampleTheme {
        ColesPayTabButton(
            isSelected = true
        ) {}
    }
}