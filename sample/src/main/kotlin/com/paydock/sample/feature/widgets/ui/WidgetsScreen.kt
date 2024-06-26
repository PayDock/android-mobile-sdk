package com.paydock.sample.feature.widgets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paydock.sample.core.presentation.ui.layout.ColumnWithSeparators
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.widgets.ui.components.WidgetRow
import com.paydock.sample.feature.widgets.ui.models.Widget
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun WidgetsScreen(onItemClick: (WidgetType) -> Unit) {
    val widgets = listOf(
        Widget(WidgetType.CREDIT_CARD_DETAILS, "Card Details", "Tokensise card details"),
        Widget(WidgetType.GIFT_CARD_DETAILS, "Gift Card Details", "Tokensise card details"),
        Widget(WidgetType.ADDRESS_DETAILS, "Address", "Capture customer address form"),
        Widget(WidgetType.GOOGLE_PAY, "Google Pay", "Standalone Google Pay button"),
        Widget(WidgetType.PAY_PAL, "PayPal", "Standalone PayPal button"),
        Widget(WidgetType.FLY_PAY, "FlyPay", "Standalone FlyPay button"),
        Widget(WidgetType.AFTER_PAY, "Afterpay", "Standalone Afterpay button"),
        Widget(WidgetType.MASTERCARD_SRC, "Mastercard SRC", "Mastercard SRC flow"),
        Widget(WidgetType.INTEGRATED_3DS, "Integrated 3DS", "Integrated 3DS flow"),
        Widget(WidgetType.STANDALONE_3DS, "Standalone 3DS", "Standalone 3DS flow")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ColumnWithSeparators(content = {
            widgets.forEach { widget: Widget ->
                WidgetRow(
                    title = widget.title,
                    description = widget.description,
                ) {
                    onItemClick.invoke(widget.type)
                }
            }
        })
    }
}

@Preview
@Composable
private fun PreviewWidgetsScreen() {
    SampleTheme {
        WidgetsScreen {}
    }
}