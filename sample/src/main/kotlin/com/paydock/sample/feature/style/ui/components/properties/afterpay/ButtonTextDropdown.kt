package com.paydock.sample.feature.style.ui.components.properties.afterpay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.afterpay.android.view.AfterpayPaymentButton
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.StringDropdown

@Composable
fun ButtonTextDropdown(
    modifier: Modifier = Modifier,
    currentButtonText: AfterpayPaymentButton.ButtonText,
    onAfterpayButtonTextChange: (AfterpayPaymentButton.ButtonText) -> Unit,
) {
    val options = remember {
        listOf(
            "Pay" to AfterpayPaymentButton.ButtonText.PAY,
            "Buy" to AfterpayPaymentButton.ButtonText.BUY,
            "Checkout" to AfterpayPaymentButton.ButtonText.CHECKOUT,
            "Place Order" to AfterpayPaymentButton.ButtonText.CONTINUE
        )
    }

    val selectedOptionString = remember(currentButtonText) {
        options.find { it.second == currentButtonText }?.first
            ?: AfterpayPaymentButton.ButtonText.DEFAULT.name
    }

    StringDropdown(
        modifier = modifier,
        title = stringResource(R.string.label_afterpay_button_text),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newButtonText =
                options.find { it.first == newValueString }?.second
                    ?: AfterpayPaymentButton.ButtonText.DEFAULT
            onAfterpayButtonTextChange(newButtonText)
        }
    )
}