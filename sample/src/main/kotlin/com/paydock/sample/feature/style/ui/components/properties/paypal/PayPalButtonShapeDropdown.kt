package com.paydock.sample.feature.style.ui.components.properties.paypal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.StringDropdown
import com.paypal.android.paymentbuttons.PaymentButtonShape

@Composable
fun PayPalButtonShapeDropdown(
    modifier: Modifier = Modifier,
    currentShape: PaymentButtonShape,
    onPayPalShapeChange: (PaymentButtonShape) -> Unit,
) {
    val options = remember {
        listOf(
            "Rectangle" to PaymentButtonShape.RECTANGLE,
            "Rounded" to PaymentButtonShape.ROUNDED,
            "Pill" to PaymentButtonShape.PILL,
        )
    }

    val selectedOptionString = remember(currentShape) {
        options.find { it.second == currentShape }?.first ?: PaymentButtonShape.ROUNDED.name
    }

    StringDropdown(
        modifier = modifier,
        title = stringResource(R.string.label_paypal_button_shape),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newShape =
                options.find { it.first == newValueString }?.second ?: PaymentButtonShape.ROUNDED
            onPayPalShapeChange(newShape)
        }
    )
}


