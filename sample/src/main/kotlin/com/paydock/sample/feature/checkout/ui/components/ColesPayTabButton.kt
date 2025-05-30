package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun ColesPayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    val selectedColor = Theme.colors.primary
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = selectedColor,
        selectedBackgroundColor = Color.White,
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