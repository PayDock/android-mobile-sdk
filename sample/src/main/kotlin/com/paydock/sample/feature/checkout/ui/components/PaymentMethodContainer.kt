package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.designsystems.theme.typography.FontFamily
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun PaymentMethodContainer(
    modifier: Modifier = Modifier,
    paymentMethods: List<WidgetType>,
    selectedTab: WidgetType,
    onTabSelected: (WidgetType) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontFamily = FontFamily,
                fontWeight = FontWeight(500)
            ),
            text = "Payment Method",
            color = Theme.colors.onPrimary
        )
        HorizontalTabButtonCarousel(
            modifier = Modifier.fillMaxWidth(),
            paymentMethods = paymentMethods,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
    }
}