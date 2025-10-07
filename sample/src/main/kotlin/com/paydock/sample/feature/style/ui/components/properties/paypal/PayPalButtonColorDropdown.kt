package com.paydock.sample.feature.style.ui.components.properties.paypal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paypal.android.paymentbuttons.PayPalButtonColor
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.dropdown.DropdownSelector

@Composable
fun PayPalButtonColorDropdown(
    modifier: Modifier = Modifier,
    currentColor: PayPalButtonColor,
    onPayPalColorChange: (PayPalButtonColor) -> Unit,
) {
    val options = remember {
        listOf(
            "Gold" to PayPalButtonColor.GOLD,
            "Blue" to PayPalButtonColor.BLUE,
            "White" to PayPalButtonColor.WHITE,
            "Black" to PayPalButtonColor.BLACK,
            "Silver" to PayPalButtonColor.SILVER,
        )
    }

    val selectedOptionString = remember(currentColor) {
        options.find { it.second == currentColor }?.first ?: PayPalButtonColor.GOLD.name
    }

    DropdownSelector(
        modifier = modifier,
        title = stringResource(R.string.label_paypal_button_color),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newColor = options.find { it.first == newValueString }?.second ?: PayPalButtonColor.GOLD
            onPayPalColorChange(newColor)
        }
    )
}


