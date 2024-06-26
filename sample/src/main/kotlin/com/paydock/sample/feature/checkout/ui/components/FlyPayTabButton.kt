package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.FlyPayBlue
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun FlyPayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = FlyPayBlue,
        selectedBackgroundColor = FlyPayBlue,
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_flypay),
            colorFilter = ColorFilter.tint(if (isSelected) Color.White else Theme.colors.onSurface),
            contentDescription = null
        )
    }
}

@Composable
@Preview
private fun FlyPayTabButtonDefault() {
    SampleTheme {
        FlyPayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@Preview
private fun FlyPayTabButtonSelected() {
    SampleTheme {
        FlyPayTabButton(
            isSelected = true
        ) {}
    }
}