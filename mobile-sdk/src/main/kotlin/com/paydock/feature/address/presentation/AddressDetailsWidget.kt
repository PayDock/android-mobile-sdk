package com.paydock.feature.address.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.ui.utils.rememberImeState
import com.paydock.designsystems.components.SdkButton
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.address.data.mapper.asEntity
import com.paydock.feature.address.domain.model.BillingAddress
import com.paydock.feature.address.presentation.components.AddressSearchInput
import com.paydock.feature.address.presentation.components.ManualAddress
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable for displaying address details input UI.
 *
 * @param modifier Modifier to apply to the composable.
 * @param address The preset address to pre-fill the input fields.
 * @param completion Callback function to execute when the address is saved.
 */
@Suppress("LongMethod")
@Composable
fun AddressDetailsWidget(
    modifier: Modifier = Modifier,
    address: BillingAddress? = null,
    completion: (BillingAddress) -> Unit
) {
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()
    // Get the ViewModel using Koin dependency injection
    val viewModel: AddressDetailsViewModel = koinViewModel()

    // Remember the preset address value to avoid recomposition on every change
    remember(address) {
        address?.let { viewModel.updateDefaultAddress(it) }
        address
    }

    // Collect the UI state from the ViewModel
    val uiState by viewModel.stateFlow.collectAsState()

    // Control whether the manual address section is shown
    var showManualAddress by remember { mutableStateOf(address != null) }

    LaunchedEffect(imeState) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    SdkTheme {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            // Header text
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = Theme.typography.body1,
                text = stringResource(R.string.label_find_an_address),
                color = Theme.colors.onSurfaceVariant
            )

            // Address search input
            AddressSearchInput(
                modifier = Modifier.testTag("addressSearch"),
                onAddressSelected = { result ->
                    showManualAddress = true
                    viewModel.updateDefaultAddress(result.asEntity())
                }
            )

            // Show the "Enter Address Manually" text
            if (!showManualAddress) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showManualAddress = !showManualAddress }
                        .testTag("showManualAddressButton"),
                    style = Theme.typography.body2.copy(textDecoration = TextDecoration.Underline),
                    text = stringResource(R.string.button_enter_address_manually),
                    color = Theme.colors.primary
                )
            }

            // Slide-down animation for the ManualAddress component
            AnimatedVisibility(
                visible = showManualAddress,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(MobileSDKConstants.General.EXPANSION_TRANSITION_DURATION)
                ) + fadeIn(
                    initialAlpha = 0.3f,
                    animationSpec = tween(MobileSDKConstants.General.EXPANSION_TRANSITION_DURATION)
                )
            ) {
                ManualAddress(
                    modifier = Modifier.testTag("manualAddress"),
                    savedAddress = uiState.billingAddress,
                    onAddressUpdated = { addressState ->
                        viewModel.updateManualAddress(addressState)
                    }
                )
            }

            // Save Address button
            SdkButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .testTag("saveAddress"),
                text = stringResource(R.string.button_save_address),
                enabled = uiState.isDataValid
            ) {
                completion(uiState.billingAddress)
            }
        }
    }
}
