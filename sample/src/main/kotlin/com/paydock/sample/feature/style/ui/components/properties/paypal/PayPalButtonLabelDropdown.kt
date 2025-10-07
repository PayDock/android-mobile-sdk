package com.paydock.sample.feature.style.ui.components.properties.paypal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paypal.android.paymentbuttons.PayPalButtonLabel
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.dropdown.DropdownSelector

@Composable
fun PayPalButtonLabelDropdown(
    modifier: Modifier = Modifier,
    currentLabel: PayPalButtonLabel,
    onPayPalLabelChange: (PayPalButtonLabel) -> Unit,
) {
    val options = remember {
        listOf(
            "PayPal (wordmark only)" to PayPalButtonLabel.PAYPAL,
            "Checkout" to PayPalButtonLabel.CHECKOUT,
            "Buy Now" to PayPalButtonLabel.BUY_NOW,
            "Pay" to PayPalButtonLabel.PAY,
            // PAY_LATER is not supported by PayPalButton, excluded intentionally
        )
    }

    val selectedOptionString = remember(currentLabel) {
        options.find { it.second == currentLabel }?.first ?: PayPalButtonLabel.PAYPAL.name
    }

    DropdownSelector(
        modifier = modifier,
        title = stringResource(R.string.label_paypal_button_label),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newLabel = options.find { it.first == newValueString }?.second ?: PayPalButtonLabel.PAYPAL
            onPayPalLabelChange(newLabel)
        }
    )
}


