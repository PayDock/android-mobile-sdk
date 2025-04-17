package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.FlyPayBlue
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun FlyPayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = FlyPayBlue,
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_flypay),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
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