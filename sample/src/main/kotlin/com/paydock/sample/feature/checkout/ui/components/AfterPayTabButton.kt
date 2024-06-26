package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.AfterPayGreen
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun AfterPayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = AfterPayGreen,
        selectedBackgroundColor = AfterPayGreen,
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
private fun AfterPayTabButtonDefault() {
    SampleTheme {
        AfterPayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@Preview
private fun AfterPayTabButtonSelected() {
    SampleTheme {
        AfterPayTabButton(
            isSelected = true
        ) {}
    }
}