package com.paydock.sample.designsystems.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.feature.address.domain.model.integration.AddressDetailsWidgetConfig
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.presentation.AddressDetailsAppearanceDefaults
import com.paydock.feature.address.presentation.AddressDetailsWidget
import com.paydock.feature.paypal.vault.domain.model.integration.ButtonIcon
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppTextButton

data class AddressWidgetResult(
    val label: String,
    val isDefault: Boolean,
    val billingAddress: BillingAddress
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressWidgetSheet(
    isEditing: Boolean,
    title: String? = null,
    initialBillingAddress: BillingAddress? = null,
    initialLabel: String? = null,
    initialIsDefault: Boolean = false,
    showDefaultToggle: Boolean = true,
    showAddressTypeChips: Boolean = true,
    onDismissRequest: () -> Unit = {},
    onSave: (AddressWidgetResult) -> Unit
) {
    val context = LocalContext.current

    var isDefault by remember(initialIsDefault) { mutableStateOf(initialIsDefault) }
    val defaultType = stringResource(R.string.type_home)
    var addressType by remember(initialLabel, defaultType) {
        mutableStateOf(
            initialLabel ?: defaultType
        )
    }
    var addressResult by remember(initialBillingAddress) { mutableStateOf(initialBillingAddress) }

    // Use a key to force widget recreation when sheet is reopened
    // This ensures the AddressDetailsWidget starts fresh each time
    var widgetKey by remember { mutableIntStateOf(0) }

    val handleDismiss = {
        // Reset addressResult to initial value and increment key on dismiss
        addressResult = initialBillingAddress
        widgetKey++
        onDismissRequest()
    }

    AppBottomSheet(
        title = title
            ?: if (isEditing) stringResource(R.string.label_edit_address) else stringResource(R.string.label_add_new_address),
        trailingContent = {
            AppTextButton(
                text = stringResource(id = R.string.button_cancel),
                onClick = handleDismiss
            )
        },
        skipPartiallyExpanded = true,
        expandOnOpen = true,
        allowOutsideDismiss = false,
        onDismissRequest = handleDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showAddressTypeChips) {
                    Text(
                        text = stringResource(R.string.label_address_type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = remember {
                            listOf(
                                context.getString(R.string.type_home),
                                context.getString(R.string.type_work),
                                context.getString(R.string.type_other)
                            )
                        }
                        types.forEach { type ->
                            AssistChip(
                                onClick = { addressType = type },
                                label = {
                                    Text(
                                        style = MaterialTheme.typography.labelSmall,
                                        text = type
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (addressType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.3f
                                    ),
                                    labelColor = if (addressType == type) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                if (showDefaultToggle) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.label_set_as_default_address),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = isDefault,
                            onCheckedChange = { isDefault = it }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Use key to force widget recreation and clear all fields including country
                // Create a new config instance each time to ensure complete widget reset
                key(widgetKey, addressResult) {
                    AddressDetailsWidget(
                        config = AddressDetailsWidgetConfig(
                            address = addressResult
                        ),
                        appearance = AddressDetailsAppearanceDefaults.appearance().copy(
                            actionButton = ButtonAppearanceDefaults
                                .outlineButtonAppearance()
                                .copy(
                                    text = if (isEditing) stringResource(R.string.button_update) else stringResource(
                                        R.string.button_add
                                    ),
                                    icon = if (isEditing) ButtonIcon.Vector(Icons.Filled.Edit) else ButtonIcon.Vector(
                                        Icons.Filled.Add
                                    )
                                )
                        )
                    ) { billingAddress ->
                        val result = AddressWidgetResult(
                            label = addressType,
                            isDefault = isDefault,
                            billingAddress = billingAddress
                        )
                        // Reset addressResult and increment key before saving to force fresh widget on next open
                        addressResult = initialBillingAddress
                        widgetKey++
                        onSave(result)
                    }
                }
            }
        }
    }
}


