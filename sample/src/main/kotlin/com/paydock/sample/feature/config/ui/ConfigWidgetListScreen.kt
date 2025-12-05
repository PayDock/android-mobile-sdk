package com.paydock.sample.feature.config.ui

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.BuildConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.list.DisplayableListItem
import com.paydock.sample.designsystems.components.list.ListRowItem
import com.paydock.sample.feature.widgets.ui.models.WidgetType

// Global Config list item
object GlobalConfigListItem : DisplayableListItem {
    override fun displayIcon(): Int? = R.drawable.ic_config
    override fun displayName(): String = "Global Config"
    override fun displayDescription(): String? = "API access token and test cart configuration"
}

@Composable
fun ConfigWidgetListScreen(
    onWidgetSelected: (WidgetType) -> Unit,
    onGlobalConfigSelected: () -> Unit = {}
) {
    // Cache filtered and sorted to avoid recomputation
    val configurableWidgets = remember {
        WidgetType.entries
            .filter { it.hasConfigurableProperties() }
            .sortedBy { it.displayName() }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        // Caption text
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                text = stringResource(R.string.label_config_screen_caption),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
            )
        }

        // Divider
        item {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // Global Config item - placed before widget list
        item {
            ListRowItem(
                title = GlobalConfigListItem.displayName(),
                description = GlobalConfigListItem.displayDescription(),
                iconResource = GlobalConfigListItem.displayIcon()
            ) {
                onGlobalConfigSelected()
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Spacer to separate Global Config from Widget section
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Widget section title
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
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

        // Spacer before version section
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Version section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "App Version",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version: ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Build: ${BuildConfig.VERSION_CODE}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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
        WidgetType.PAY_PAL,
        WidgetType.PAY_PAL_VAULT,
        WidgetType.AFTER_PAY,
        WidgetType.COLES_PAY,
        WidgetType.CLICK_TO_PAY -> true

        else -> false
    }
}

