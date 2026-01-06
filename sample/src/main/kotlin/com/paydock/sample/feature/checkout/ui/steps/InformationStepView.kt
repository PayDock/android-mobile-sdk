package com.paydock.sample.feature.checkout.ui.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppTextButton
import com.paydock.sample.designsystems.components.common.DefaultBadge
import com.paydock.sample.feature.checkout.domain.model.Address
import com.paydock.sample.feature.checkout.domain.model.AddressType
import com.paydock.sample.feature.checkout.domain.model.SavedAddress
import com.paydock.sample.feature.checkout.presentation.EnhancedCheckoutViewModel

@Composable
fun InformationStepView(
    viewModel: EnhancedCheckoutViewModel
) {
    val savedAddresses by viewModel.savedAddresses.collectAsState()

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.testTag("information_step_view")
    ) {
        // Profile Auto-fill Section
        if (viewModel.contactInfo.firstName.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_autofill_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Welcome back, ${viewModel.contactInfo.firstName}!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("profile_welcome_text")
                    )

                    AppTextButton(
                        text = "Use Profile Info",
                        onClick = { viewModel.loadProfileData() },
                        modifier = Modifier.testTag("profile_use_info_button")
                    )
                }
            }
        }

        // Contact Information Section
        ContactInformationSection(viewModel = viewModel)

        // Shipping Address Section
        ShippingAddressSection(
            viewModel = viewModel,
            savedAddresses = savedAddresses
        )

        // Use as Billing Address Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("use_shipping_as_billing_row"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Use as billing address",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("use_shipping_as_billing_label")
            )
            Switch(
                checked = viewModel.useShippingAsBilling,
                onCheckedChange = { viewModel.setShippingAsBilling(it) },
                modifier = Modifier.testTag("use_shipping_as_billing_switch")
            )
        }

        // Billing Address Section (only if not using shipping as billing)
        if (!viewModel.useShippingAsBilling) {
            BillingAddressSection(
                viewModel = viewModel,
                savedAddresses = savedAddresses
            )
        }
    }
}

@Composable
private fun ContactInformationSection(
    viewModel: EnhancedCheckoutViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.testTag("contact_information_section")
    ) {
        Text(
            text = "Contact Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("contact_information_title")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.contactInfo.firstName,
                    onValueChange = { viewModel.updateContactInfo(firstName = it) },
                    label = { Text("First Name") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("contact_first_name_field")
                )

                OutlinedTextField(
                    value = viewModel.contactInfo.lastName,
                    onValueChange = { viewModel.updateContactInfo(lastName = it) },
                    label = { Text("Last Name") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("contact_last_name_field")
                )
            }

            OutlinedTextField(
                value = viewModel.contactInfo.email,
                onValueChange = { viewModel.updateContactInfo(email = it) },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_email_field")
            )

            OutlinedTextField(
                value = viewModel.contactInfo.phone,
                onValueChange = { viewModel.updateContactInfo(phone = it) },
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_phone_field")
            )
        }
    }
}

@Composable
private fun ShippingAddressSection(
    viewModel: EnhancedCheckoutViewModel,
    savedAddresses: List<SavedAddress>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.testTag("shipping_address_section")
    ) {
        Text(
            text = "Shipping Address",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("shipping_address_title")
        )

        // Saved Address Selection
        if (savedAddresses.isNotEmpty()) {
            // Auto-select default if none selected yet
            LaunchedEffect(savedAddresses, viewModel.selectedShippingAddressId) {
                if (viewModel.selectedShippingAddressId == null) {
                    savedAddresses.firstOrNull { it.isDefault }?.let { def ->
                        viewModel.selectSavedAddress(def, AddressType.SHIPPING)
                    }
                }
            }
            SavedAddressesSection(
                savedAddresses = savedAddresses,
                selectedAddressId = viewModel.selectedShippingAddressId,
                onAddressSelected = { savedAddress ->
                    viewModel.selectSavedAddress(savedAddress, AddressType.SHIPPING)
                },
                onEnterNewAddress = {
                    // Open empty sheet (no prefill)
                    viewModel.showAddressWidget(AddressType.SHIPPING, initialAddress = null)
                },
                onEditAddress = { savedAddress ->
                    viewModel.startEditingSavedAddress(savedAddress, AddressType.SHIPPING)
                }
            )
        }

        // Address Input Section
        if (viewModel.selectedShippingAddressId == null) {
            AddressInputSection(
                address = viewModel.shippingAddress,
                addressType = AddressType.SHIPPING,
                onEnterAddress = {
                    viewModel.showAddressWidget(
                        AddressType.SHIPPING,
                        initialAddress = viewModel.shippingAddress.takeIf { it.isComplete })
                }
            )
        }
    }
}

@Composable
private fun BillingAddressSection(
    viewModel: EnhancedCheckoutViewModel,
    savedAddresses: List<SavedAddress>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.testTag("billing_address_section")
    ) {
        Text(
            text = "Billing Address",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("billing_address_title")
        )

        // Saved Address Selection for Billing
        if (savedAddresses.isNotEmpty()) {
            SavedAddressesSection(
                savedAddresses = savedAddresses,
                selectedAddressId = viewModel.selectedBillingAddressId,
                onAddressSelected = { savedAddress ->
                    viewModel.selectSavedAddress(savedAddress, AddressType.BILLING)
                },
                onEnterNewAddress = {
                    viewModel.showAddressWidget(AddressType.BILLING, initialAddress = null)
                },
                onEditAddress = { savedAddress ->
                    viewModel.startEditingSavedAddress(savedAddress, AddressType.BILLING)
                }
            )
        }

        // Billing Address Input Section
        if (viewModel.selectedBillingAddressId == null) {
            AddressInputSection(
                address = viewModel.billingAddress,
                addressType = AddressType.BILLING,
                onEnterAddress = {
                    viewModel.showAddressWidget(
                        AddressType.BILLING,
                        initialAddress = viewModel.billingAddress.takeIf { it.isComplete })
                }
            )
        }
    }
}

@Composable
private fun SavedAddressesSection(
    savedAddresses: List<SavedAddress>,
    selectedAddressId: String?,
    onAddressSelected: (SavedAddress) -> Unit,
    onEnterNewAddress: () -> Unit,
    onEditAddress: (SavedAddress) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_addresses_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Addresses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AppTextButton(
                    text = stringResource(R.string.button_add_address),
                    onClick = onEnterNewAddress,
                    modifier = Modifier.testTag("add_new_address_button")
                )
            }

            savedAddresses.forEach { address ->
                SavedAddressRow(
                    address = address,
                    isSelected = selectedAddressId == address.id,
                    onSelect = { onAddressSelected(address) },
                    onEdit = { onEditAddress(address) }
                )
            }
        }
    }
}

@Composable
private fun SavedAddressRow(
    address: SavedAddress,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp)
            .testTag("saved_address_row_${address.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = if (isSelected) "Selected" else "Not selected",
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = address.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (address.isDefault) {
                    DefaultBadge()
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("saved_address_edit_${address.id}")
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit address")
                }
            }
            // Show name if available
            val fullName = listOfNotNull(
                address.firstName?.takeIf { it.isNotBlank() },
                address.lastName?.takeIf { it.isNotBlank() }
            ).joinToString(" ").trim()
            if (fullName.isNotEmpty()) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = address.formattedAddress.replace("\n", ", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun AddressInputSection(
    address: Address,
    addressType: AddressType,
    onEnterAddress: () -> Unit
) {
    if (address.isComplete) {
        // Show filled address with edit option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("address_display_card_${addressType.name.lowercase()}"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Box {
                IconButton(
                    onClick = onEnterAddress,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .testTag("address_edit_button_${addressType.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit address",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${if (addressType == AddressType.SHIPPING) "Shipping" else "Billing"} Address:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("address_display_label_${addressType.name.lowercase()}")
                    )

                    Text(
                        text = address.formattedAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("address_display_text_${addressType.name.lowercase()}")
                    )
                }
            }
        }
    } else {
        // Show enter address button
        AppButton(
            text = "Add ${if (addressType == AddressType.SHIPPING) "Shipping" else "Billing"} Address",
            onClick = onEnterAddress,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_address_button_${addressType.name.lowercase()}")
        )
    }
}