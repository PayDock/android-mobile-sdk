package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.designsystems.theme.typography.FontFamily

@Composable
fun ClickToPayTabButton(isSelected: Boolean, onClick: () -> Unit) {
    val selectedColor = Theme.colors.primary
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = selectedColor,
        selectedBackgroundColor = Color.White,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_src),
            contentDescription = null,
            tint = if (isSelected) Theme.colors.primary else Theme.colors.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.label_click_to_pay),
            color = if (isSelected) selectedColor else Theme.colors.onSurface,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 16.8.sp,
                fontFamily = FontFamily,
                fontWeight = FontWeight(500)
            )
        )
    }
}

@Composable
@Preview
private fun CardTabButtonDefault() {
    SampleTheme {
        ClickToPayTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@Preview
private fun CardTabButtonSelected() {
    SampleTheme {
        ClickToPayTabButton(
            isSelected = true
        ) {}
    }
}