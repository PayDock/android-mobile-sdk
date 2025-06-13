package com.paydock.sample.feature.style.ui.components.properties.afterpay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.afterpay.android.view.AfterpayPaymentButton
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.dropdown.DropdownSelector

@Composable
fun ButtonTextDropdown(
    modifier: Modifier = Modifier,
    currentButtonText: AfterpayPaymentButton.ButtonText,
    onAfterpayButtonTextChange: (AfterpayPaymentButton.ButtonText) -> Unit,
) {
    val options = remember {
        listOf(
            "Pay Now" to AfterpayPaymentButton.ButtonText.PAY_NOW,
            "Checkout" to AfterpayPaymentButton.ButtonText.CHECKOUT,
            "Buy Now" to AfterpayPaymentButton.ButtonText.BUY_NOW,
            "Place Order" to AfterpayPaymentButton.ButtonText.PLACE_ORDER
        )
    }

    val selectedOptionString = remember(currentButtonText) {
        options.find { it.second == currentButtonText }?.first
            ?: AfterpayPaymentButton.ButtonText.PAY_NOW.name
    }

    DropdownSelector(
        modifier = modifier,
        title = stringResource(R.string.label_afterpay_button_text),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newButtonText =
                options.find { it.first == newValueString }?.second
                    ?: AfterpayPaymentButton.ButtonText.PAY_NOW
            onAfterpayButtonTextChange(newButtonText)
        }
    )
}