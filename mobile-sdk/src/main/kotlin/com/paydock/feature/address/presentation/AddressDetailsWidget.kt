package com.paydock.feature.address.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.ui.utils.rememberImeState
import com.paydock.designsystems.components.button.SdkButton
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.address.domain.mapper.integration.asEntity
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.presentation.components.AddressSearchSection
import com.paydock.feature.address.presentation.components.ManualAddressEntry
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable for displaying address details input UI.
 *
 * @param modifier Modifier to apply to the composable.
 * @param address The preset address to pre-fill the input fields.
 * @param completion Callback function to execute when the address is saved.
 */
@Composable
fun AddressDetailsWidget(
    modifier: Modifier = Modifier,
    address: BillingAddress? = null,
    completion: (BillingAddress) -> Unit,
) {
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()
    // Get the ViewModel using Koin dependency injection
    val viewModel: AddressDetailsViewModel = koinViewModel()

    // Collect the UI state from the ViewModel
    val uiState by viewModel.stateFlow.collectAsState()

    // Control whether the manual address section is shown
    var isManualAddressVisible by remember { mutableStateOf(address != null) }
    // Control whether the manual address input is valid (improve recompositions)
    val isDataValid by remember(uiState) { derivedStateOf { uiState.isDataValid } }

    // Remember the preset address value to avoid recomposition on every change
    LaunchedEffect(address) {
        address?.let(viewModel::updateDefaultAddress)
    }

    LaunchedEffect(imeState) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    SdkTheme {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Theme.colors.background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            AddressSearchSection { address ->
                isManualAddressVisible = true
                viewModel.updateDefaultAddress(address.asEntity())
            }

            // Show the "Enter Address Manually" text
            if (!isManualAddressVisible) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isManualAddressVisible = !isManualAddressVisible }
                        .testTag("showManualAddressButton"),
                    style = Theme.typography.body2.copy(textDecoration = TextDecoration.Underline),
                    text = stringResource(R.string.button_enter_address_manually),
                    color = Theme.colors.primary
                )
            }

            ManualAddressEntry(
                isManualAddressVisible = isManualAddressVisible,
                address = uiState.billingAddress,
            ) { addressState ->
                viewModel.updateManualAddress(addressState)
            }

            // Save Address button
            SdkButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .testTag("saveAddress"),
                text = stringResource(R.string.button_save_address),
                enabled = isDataValid
            ) {
                completion(uiState.billingAddress)
            }
        }
    }
}