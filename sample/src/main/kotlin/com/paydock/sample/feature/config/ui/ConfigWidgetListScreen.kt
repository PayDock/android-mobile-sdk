package com.paydock.sample.feature.config.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.list.ListRowItem
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun ConfigWidgetListScreen(
    onWidgetSelected: (WidgetType) -> Unit,
    onCartSelected: () -> Unit = {}
) {
    // Cache filtered and sorted to avoid recomputation
    val configurableWidgets = remember {
        WidgetType.entries
            .filter { it.hasConfigurableProperties() }
            .sortedBy { it.displayName() }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {

        // Cart item - placed before widget list
        item {
            ListRowItem(
                title = "Cart",
                description = "Cart amount configuration",
                iconResource = R.drawable.ic_config
            ) {
                onCartSelected()
            }
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Widget section title
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                text = stringResource(R.string.label_config_widget_list_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        // Widget items
        items(
            items = configurableWidgets,
            key = { it.name }
        ) { widgetType ->
            ListRowItem(
                title = widgetType.displayName(),
                description = widgetType.displayDescription(),
                iconResource = widgetType.displayIcon()
            ) {
                onWidgetSelected(widgetType)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun WidgetType.hasConfigurableProperties(): Boolean {
    return when (this) {
        WidgetType.ADDRESS_DETAILS,
        WidgetType.CARD_DETAILS,
        WidgetType.GIFT_CARD,
        WidgetType.GOOGLE_PAY,
        WidgetType.PAY_PAL,
        WidgetType.PAY_PAL_VAULT,
        WidgetType.AFTER_PAY,
        WidgetType.COLES_PAY,
        WidgetType.CLICK_TO_PAY,
        WidgetType.ZIP -> true

        else -> false
    }
}

