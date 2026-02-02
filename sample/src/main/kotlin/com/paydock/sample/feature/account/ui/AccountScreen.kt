package com.paydock.sample.feature.account.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.presentation.PayPalPaymentSourceAppearanceDefaults
import com.paydock.feature.paypal.vault.presentation.PayPalSavePaymentSourceWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppButtonShape
import com.paydock.sample.designsystems.components.button.AppButtonVariant
import com.paydock.sample.designsystems.components.button.AppTextButton
import com.paydock.sample.designsystems.components.common.DefaultBadge
import com.paydock.sample.designsystems.components.common.SectionCard
import com.paydock.sample.designsystems.components.common.SectionHeader
import com.paydock.sample.designsystems.components.sheets.AddressWidgetResult
import com.paydock.sample.designsystems.components.sheets.AddressWidgetSheet
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.account.AccountViewModel
import com.paydock.sample.feature.account.data.UserProfileManager
import com.paydock.sample.feature.account.domain.model.Customer
import com.paydock.sample.feature.account.domain.model.SavedAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(viewModel: AccountViewModel = hiltViewModel<AccountViewModel>()) {
    val profileManager = remember { UserProfileManager.shared }
    val profile by profileManager.profile.collectAsState()
    val context = LocalContext.current

    val uiState by viewModel.stateFlow.collectAsState()

    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var showAddressWidget by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<SavedAddress?>(null) }

    // Editable profile fields
    var firstName by remember { mutableStateOf(profile.firstName) }
    var lastName by remember { mutableStateOf(profile.lastName) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }

    // Update fields when profile changes
    LaunchedEffect(profile) {
        firstName = profile.firstName
        lastName = profile.lastName
        email = profile.email
        phone = profile.phone
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Profile Info Section
        val canSaveProfile = firstName != profile.firstName ||
                lastName != profile.lastName ||
                email != profile.email ||
                phone != profile.phone
        ProfileInfoSection(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            onFirstNameChange = { firstName = it },
            onLastNameChange = { lastName = it },
            onEmailChange = { email = it },
            onPhoneChange = { phone = it },
            onSaveChanges = {
                profileManager.updateProfile(firstName, lastName, email, phone)
                alertMessage = context.getString(R.string.msg_profile_updated)
                showAlert = true
            },
            canSave = canSaveProfile
        )

        // Saved Payment Methods Section
        SavedPaymentMethodsSection(
            viewModel = viewModel,
            customer = uiState.customer
        )

        // Saved Addresses Section
        SavedAddressesSection(
            savedAddresses = profile.savedAddresses,
            fullName = listOfNotNull(profile.firstName, profile.lastName).joinToString(" ")
                .trim(),
            onAddAddress = {
                editingAddress = null
                showAddressWidget = true
            },
            onEditAddress = { address ->
                editingAddress = address
                showAddressWidget = true
            },
            onDeleteAddress = { address ->
                profileManager.deleteAddress(address)
                alertMessage = context.getString(R.string.msg_address_deleted)
                showAlert = true
            },
            onSetDefault = { address ->
                profileManager.setAddressAsDefault(address)
                alertMessage = context.getString(R.string.msg_default_address_updated)
                showAlert = true
            }
        )
    }

    // Address Widget Modal (shared)
    if (showAddressWidget) {
        val isEditing = editingAddress != null
        AddressWidgetSheet(
            isEditing = isEditing,
            initialBillingAddress = editingAddress?.let {
                BillingAddress(
                    firstName = it.firstName,
                    lastName = it.lastName,
                    addressLine1 = it.addressLine1,
                    addressLine2 = it.addressLine2,
                    city = it.city,
                    state = it.state,
                    postalCode = it.postalCode,
                    country = it.country
                )
            },
            initialLabel = editingAddress?.label,
            initialIsDefault = editingAddress?.isDefault ?: false,
            onDismissRequest = {
                editingAddress = null
                showAddressWidget = false
            },
            onSave = { result: AddressWidgetResult ->
                val savedAddress = SavedAddress(
                    id = editingAddress?.id ?: SavedAddress.generateId(),
                    label = result.label,
                    firstName = result.billingAddress.firstName ?: "",
                    lastName = result.billingAddress.lastName ?: "",
                    addressLine1 = result.billingAddress.addressLine1 ?: "",
                    addressLine2 = result.billingAddress.addressLine2 ?: "",
                    city = result.billingAddress.city ?: "",
                    state = result.billingAddress.state ?: "",
                    postalCode = result.billingAddress.postalCode ?: "",
                    country = result.billingAddress.country ?: "",
                    isDefault = result.isDefault
                )
                if (isEditing) {
                    profileManager.updateAddress(savedAddress)
                    alertMessage = context.getString(R.string.msg_address_updated)
                } else {
                    profileManager.addAddress(savedAddress)
                    alertMessage = context.getString(R.string.msg_address_added)
                }
                showAlert = true
                editingAddress = null
                showAddressWidget = false
            }
        )
    }

    // Alert Dialog
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text(stringResource(R.string.title_profile)) },
            text = { Text(alertMessage) },
            confirmButton = {
                AppTextButton(text = stringResource(R.string.button_ok)) { showAlert = false }
            }
        )
    }
}

@Composable
private fun ProfileInfoSection(
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSaveChanges: () -> Unit,
    canSave: Boolean
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(title = stringResource(R.string.label_personal_information))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = onFirstNameChange,
                        label = { Text(stringResource(R.string.label_first_name)) },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = onLastNameChange,
                        label = { Text(stringResource(R.string.label_last_name)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(R.string.label_email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text(stringResource(R.string.label_phone)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AppButton(
                text = stringResource(R.string.button_save_changes),
                onClick = onSaveChanges,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SavedPaymentMethodsSection(
    viewModel: AccountViewModel,
    customer: Customer?,
) {
    var isLoading by remember { mutableStateOf(false) }
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(title = stringResource(R.string.label_saved_payment_methods))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                var paypalError by remember { mutableStateOf<String?>(null) }
                var paypalSuccessEmail by remember { mutableStateOf<String?>(null) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // (moved) status rows will render at the bottom

                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_paypal),
                                contentDescription = "PayPal"
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Text(
                            text = stringResource(R.string.desc_link_your_paypal_account),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 4.dp)
                        )

                        // PayPal Vault Widget
                        PayPalSavePaymentSourceWidget(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            config = PayPalVaultConfig(
                                accessToken = BuildConfig.ACCESS_TOKEN_WIDGET,
                                gatewayId = BuildConfig.SERVICE_ID_PAYPAL
                            ),
                            appearance = PayPalPaymentSourceAppearanceDefaults.appearance().copy(
                                actionButton = ButtonAppearanceDefaults
                                    .outlineButtonAppearance()
                                    .copy(
                                        text = stringResource(id = R.string.button_link_account),
                                    )
                            ),
                            loadingDelegate = object : WidgetLoadingDelegate {
                                override fun widgetLoadingDidStart() {
                                    isLoading = true
                                    paypalError = null
                                    paypalSuccessEmail = null
                                }

                                override fun widgetLoadingDidFinish() {
                                    isLoading = false
                                }
                            },
                            completion = { result ->
                                result.onSuccess { vaultResult ->
                                    paypalError = null
                                    paypalSuccessEmail = vaultResult.email
                                    viewModel.createCustomer(vaultResult.token)
                                }.onFailure { throwable ->
                                    paypalSuccessEmail = null
                                    paypalError = throwable.toError().displayableMessage
                                }
                            }
                        )

                        // Bottom-aligned status area
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Success details: Name / Email rows if available
                            val fullName = listOfNotNull(
                                customer?.firstName?.takeIf { it.isNotBlank() },
                                customer?.lastName?.takeIf { it.isNotBlank() }
                            ).joinToString(" ").trim().ifBlank { null }
                            val emailToShow =
                                (customer?.email?.takeIf { it.isNotBlank() } ?: paypalSuccessEmail)

                            if (fullName != null || !emailToShow.isNullOrBlank()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.label_linked_account_details),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    fullName?.let { name ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Name",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    emailToShow?.let { email ->
                                        if (email.isNotBlank()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Email",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = email,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Error message scoped to this flow
                            paypalError?.let { message ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.errorContainer,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                    // Full-size overlay loader that does not affect layout
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedAddressesSection(
    savedAddresses: List<SavedAddress>,
    fullName: String,
    onAddAddress: () -> Unit,
    onEditAddress: (SavedAddress) -> Unit,
    onDeleteAddress: (SavedAddress) -> Unit,
    onSetDefault: (SavedAddress) -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(
                title = stringResource(R.string.label_saved_addresses),
                trailingContent = {
                    AppTextButton(
                        text = stringResource(R.string.button_add_address),
                        onClick = onAddAddress
                    )
                }
            )

            if (savedAddresses.isEmpty()) {
                EmptyAddressesView(onAddAddress = onAddAddress)
            } else {
                savedAddresses.forEach { address ->
                    SavedAddressCardView(
                        address = address,
                        fullName = fullName,
                        onEdit = { onEditAddress(address) },
                        onDelete = { onDeleteAddress(address) },
                        onSetDefault = { onSetDefault(address) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAddressesView(
    onAddAddress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = stringResource(R.string.label_no_saved_addresses),
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Text(
            text = stringResource(R.string.label_no_saved_addresses),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Text(
            text = stringResource(R.string.label_add_address_to_speed_up_checkout),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )

        AppButton(
            text = stringResource(R.string.button_add_your_first_address),
            onClick = onAddAddress,
            variant = AppButtonVariant.Outline,
            shape = AppButtonShape.Pill,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun SavedAddressCardView(
    address: SavedAddress,
    fullName: String? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (address.isDefault) 2.dp else 0.dp,
                color = if (address.isDefault) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = address.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (address.isDefault) {
                        DefaultBadge()
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.label_more_options),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.button_edit)) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )

                        if (!address.isDefault) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.button_set_as_default)) },
                                onClick = {
                                    showMenu = false
                                    onSetDefault()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.button_delete)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Show name if available
            val fullName = listOfNotNull(
                address.firstName.takeIf { it.isNotBlank() },
                address.lastName.takeIf { it.isNotBlank() }).joinToString(" ").trim()
            if (fullName.isNotEmpty()) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = address.formattedAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!address.isComplete) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = stringResource(R.string.label_incomplete),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = stringResource(R.string.label_address_incomplete),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewAccountScreen() {
    SampleTheme {
        AccountScreen()
    }
}