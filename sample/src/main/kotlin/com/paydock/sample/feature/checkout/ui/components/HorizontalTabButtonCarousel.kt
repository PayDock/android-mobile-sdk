package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun HorizontalTabButtonCarousel(
    modifier: Modifier = Modifier,
    paymentMethods: List<WidgetType>,
    selectedTab: WidgetType,
    onTabSelected: (WidgetType) -> Unit,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val scrollTabIndex by remember { mutableIntStateOf(-1) }

    // Side effect to scroll to the selected tab index
    LaunchedEffect(selectedTab, paymentMethods) {
        val index = paymentMethods.indexOf(selectedTab)
        if (index != -1 && index != scrollTabIndex) {
            lazyListState.animateScrollToItem(index)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        // Horizontal Scrollable Row of Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                12.dp,
                Alignment.Start
            ),
            state = lazyListState
        ) {
            itemsIndexed(paymentMethods) { _, tab ->
                when (tab) {
                    WidgetType.CREDIT_CARD_DETAILS -> CardTabButton(isSelected = tab == selectedTab) {
                        onTabSelected(tab)
                    }

                    WidgetType.CLICK_TO_PAY -> ClickToPayTabButton(isSelected = tab == selectedTab) {
                        onTabSelected(tab)
                    }

                    WidgetType.GOOGLE_PAY -> GooglePayTabButton(isSelected = tab == selectedTab) {
                        onTabSelected(tab)
                    }

                    WidgetType.PAY_PAL -> PayPalTabButton(isSelected = tab == selectedTab) {
                        onTabSelected(tab)
                    }

                    WidgetType.FLY_PAY -> FlyPayTabButton(isSelected = tab == selectedTab) {
                        onTabSelected(tab)
                    }

                    WidgetType.AFTER_PAY -> AfterpayTabButton(isSelected = tab == selectedTab) {
                        onTabSelected(tab)
                    }

                    else -> Unit
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HorizontalTabButtonCarouselPreview() {
    val supportedPaymentMethods =
        listOf(
            WidgetType.CREDIT_CARD_DETAILS,
            WidgetType.GOOGLE_PAY,
            WidgetType.PAY_PAL,
            WidgetType.FLY_PAY
        )
    SampleTheme {
        HorizontalTabButtonCarousel(
            modifier = Modifier.fillMaxWidth(),
            paymentMethods = supportedPaymentMethods,
            selectedTab = supportedPaymentMethods.first(),
            onTabSelected = {}
        )
    }
}
