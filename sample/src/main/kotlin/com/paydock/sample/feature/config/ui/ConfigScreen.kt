package com.paydock.sample.feature.config.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.BuildConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.list.DisplayableListItem
import com.paydock.sample.designsystems.components.list.ListRowItem

// Global Config list item
object GlobalConfigListItem : DisplayableListItem {
    override fun displayIcon(): Int = R.drawable.ic_config
    override fun displayName(): String = "Global Config"
    override fun displayDescription(): String = "API access token and test cart configuration"
}

// Checkout Config list item
object CheckoutConfigListItem : DisplayableListItem {
    override fun displayIcon(): Int = R.drawable.ic_checkout
    override fun displayName(): String = "Checkout"
    override fun displayDescription(): String = "Checkout flow configuration"
}

// Widgets Config list item
object WidgetsConfigListItem : DisplayableListItem {
    override fun displayIcon(): Int = R.drawable.ic_widgets
    override fun displayName(): String = "Widgets"
    override fun displayDescription(): String = "Configure individual payment widgets"
}

@Composable
fun ConfigScreen(
    onGlobalConfigSelected: () -> Unit = {},
    onCheckoutConfigSelected: () -> Unit = {},
    onWidgetsConfigSelected: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("config_screen")
    ) {
        // Caption text
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .testTag("config_caption_text"),
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

        // Global Config item
        item {
            ListRowItem(
                title = GlobalConfigListItem.displayName(),
                modifier = Modifier.testTag("config_global_config_item"),
                description = GlobalConfigListItem.displayDescription(),
                iconResource = GlobalConfigListItem.displayIcon()
            ) {
                onGlobalConfigSelected()
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Checkout Config item
        item {
            ListRowItem(
                title = CheckoutConfigListItem.displayName(),
                modifier = Modifier.testTag("config_checkout_config_item"),
                description = CheckoutConfigListItem.displayDescription(),
                iconResource = CheckoutConfigListItem.displayIcon()
            ) {
                onCheckoutConfigSelected()
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Widgets Config item
        item {
            ListRowItem(
                title = WidgetsConfigListItem.displayName(),
                modifier = Modifier.testTag("config_widgets_config_item"),
                description = WidgetsConfigListItem.displayDescription(),
                iconResource = WidgetsConfigListItem.displayIcon()
            ) {
                onWidgetsConfigSelected()
            }
        }

        item {
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
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .testTag("config_version_section"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "App Version",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("config_version_title")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version: ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("config_version_name")
                )
                Text(
                    text = "Build: ${BuildConfig.VERSION_CODE}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("config_version_code")
                )
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

