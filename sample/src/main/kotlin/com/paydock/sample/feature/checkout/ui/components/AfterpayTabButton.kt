package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.AfterpayGreen
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun AfterpayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = AfterpayGreen,
        selectedBackgroundColor = AfterpayGreen,
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_afterpay),
            contentDescription = null
        )
    }
}

@Composable
@Preview
private fun AfterpayTabButtonDefault() {
    SampleTheme {
        AfterpayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@Preview
private fun AfterpayTabButtonSelected() {
    SampleTheme {
        AfterpayTabButton(
            isSelected = true
        ) {}
    }
}