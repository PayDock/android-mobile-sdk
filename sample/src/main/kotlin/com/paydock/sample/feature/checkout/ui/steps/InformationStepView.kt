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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppTextButton
import com.paydock.sample.designsystems.components.common.DefaultBadge
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.checkout.domain.model.Address
import com.paydock.sample.feature.checkout.domain.model.AddressType
import com.paydock.sample.feature.checkout.domain.model.SavedAddress
import com.paydock.sample.feature.checkout.presentation.EnhancedCheckoutViewModel

@Composable
fun InformationStepView(
    viewModel: EnhancedCheckoutViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Profile Auto-fill Section
        if (viewModel.contactInfo.firstName.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        color = MaterialTheme.colorScheme.primary
                    )

                    AppTextButton(
                        text = "Use Profile Info",
                        onClick = { viewModel.loadProfileData() })
                }
            }
        }
        
        // Contact Information Section
        ContactInformationSection(viewModel = viewModel)
        
        // Shipping Address Section
        ShippingAddressSection(viewModel = viewModel)
        
        // Use as Billing Address Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Use as billing address",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = viewModel.useShippingAsBilling,
                onCheckedChange = { viewModel.setShippingAsBilling(it) }
            )
        }
        
        // Billing Address Section (only if not using shipping as billing)
        if (!viewModel.useShippingAsBilling) {
            BillingAddressSection(viewModel = viewModel)
        }
    }
}

@Composable
private fun ContactInformationSection(
    viewModel: EnhancedCheckoutViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Contact Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
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
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = viewModel.contactInfo.lastName,
                    onValueChange = { viewModel.updateContactInfo(lastName = it) },
                    label = { Text("Last Name") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            OutlinedTextField(
                value = viewModel.contactInfo.email,
                onValueChange = { viewModel.updateContactInfo(email = it) },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = viewModel.contactInfo.phone,
                onValueChange = { viewModel.updateContactInfo(phone = it) },
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ShippingAddressSection(
    viewModel: EnhancedCheckoutViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Shipping Address",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Saved Address Selection
        if (viewModel.savedAddresses.isNotEmpty()) {
            // Auto-select default if none selected yet
            LaunchedEffect(viewModel.savedAddresses, viewModel.selectedShippingAddressId) {
                if (viewModel.selectedShippingAddressId == null) {
                    viewModel.savedAddresses.firstOrNull { it.isDefault }?.let { def ->
                        viewModel.selectSavedAddress(def, AddressType.SHIPPING)
                    }
                }
            }
            SavedAddressesSection(
                savedAddresses = viewModel.savedAddresses,
                selectedAddressId = viewModel.selectedShippingAddressId,
                onAddressSelected = { savedAddress ->
                    viewModel.selectSavedAddress(savedAddress, AddressType.SHIPPING)
                },
                onEnterNewAddress = {
                    // Open empty sheet (no prefill)
                    viewModel.showAddressWidget(AddressType.SHIPPING, initialAddress = null)
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
    viewModel: EnhancedCheckoutViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Billing Address",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Saved Address Selection for Billing
        if (viewModel.savedAddresses.isNotEmpty()) {
            SavedAddressesSection(
                savedAddresses = viewModel.savedAddresses,
                selectedAddressId = viewModel.selectedBillingAddressId,
                onAddressSelected = { savedAddress ->
                    viewModel.selectSavedAddress(savedAddress, AddressType.BILLING)
                },
                onEnterNewAddress = {
                    viewModel.showAddressWidget(AddressType.BILLING, initialAddress = null)
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
    onEnterNewAddress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    onClick = onEnterNewAddress
                )
            }

            savedAddresses.forEach { address ->
                SavedAddressRow(
                    address = address,
                    isSelected = selectedAddressId == address.id,
                    onSelect = { onAddressSelected(address) }
                )
            }
        }
    }
}

@Composable
private fun SavedAddressRow(
    address: SavedAddress,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
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
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Box {
                IconButton(
                    onClick = onEnterAddress,
                    modifier = Modifier.align(Alignment.TopEnd)
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
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = address.formattedAddress,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    } else {
        // Show enter address button
        AppButton(
            text = "Add ${if (addressType == AddressType.SHIPPING) "Shipping" else "Billing"} Address",
            onClick = onEnterAddress,
            height = 50.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}