package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun PaymentMethodContainer(
    modifier: Modifier = Modifier,
    paymentMethods: List<WidgetType>,
    selectedTab: WidgetType,
    onTabSelected: (WidgetType) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            text = "Payment Method"
        )
        HorizontalTabButtonCarousel(
            modifier = Modifier.fillMaxWidth(),
            paymentMethods = paymentMethods,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
    }
}