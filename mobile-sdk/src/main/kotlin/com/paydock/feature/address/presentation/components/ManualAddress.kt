package com.paydock.feature.address.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.presentation.state.AddressDetailsInputState
import com.paydock.feature.address.presentation.viewmodels.ManualAddressViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

/**
 * Displays a manual address entry form with input fields for the user to update their address.
 *
 * @param modifier [Modifier] to apply customizations to the layout of this component.
 * @param viewModel The [ManualAddressViewModel] responsible for managing the address input state.
 * Defaults to a Koin-injected instance via [koinViewModel()].
 * @param savedAddress The optional [BillingAddress] to populate the input fields initially.
 * If provided, it is used to prefill the address fields.
 * @param onAddressUpdated A callback invoked whenever the address input state is updated.
 * The updated state is passed as an [AddressDetailsInputState] after a debounce delay.
 *
 * This function provides:
 * - Input fields for address components such as Address Line 1, Address Line 2, City, State, Postal Code, and Country.
 * - Autofill support for each field to enhance user experience.
 * - Keyboard navigation between fields using [FocusRequester].
 * - A debounce mechanism to reduce frequent updates to the `onAddressUpdated` callback.
 *
 * Example:
 * ```
 * ManualAddress(
 *     savedAddress = existingBillingAddress,
 *     onAddressUpdated = { updatedState ->
 *         println("Updated Address Details: $updatedState")
 *     }
 * )
 * ```
 *
 * Notes:
 * - The `uiState` is collected from the [ManualAddressViewModel]'s `stateFlow` to reflect user inputs.
 * - Autofill types are configured for each input field to match their specific purpose (e.g., street, locality).
 * - A [CountryInputAutoComplete] is used for country selection with a dropdown.
 * - The callback invocation is delayed using [LaunchedEffect] to avoid excessive calls during rapid user input.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongMethod")
@Composable
internal fun ManualAddress(
    modifier: Modifier = Modifier,
    viewModel: ManualAddressViewModel = koinViewModel(),
    savedAddress: BillingAddress? = null,
    onAddressUpdated: (AddressDetailsInputState) -> Unit
) {
    // Remember the selected item value to avoid recomposition on every change
    remember(savedAddress) {
        savedAddress?.let { viewModel.setSavedAddress(it) }
        savedAddress
    }

    // Collect the state from the view model
    val uiState by viewModel.stateFlow.collectAsState()

    // Create a vertical column layout for address input fields
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        // Focus requesters for managing keyboard navigation
        val focusAddressLine2 = FocusRequester()
        val focusCity = FocusRequester()
        val focusState = FocusRequester()
        val focusPostalCode = FocusRequester()

        // Address input fields
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("addressLine1Input"),
            value = uiState.addressLine1,
            label = stringResource(R.string.label_address_line_1),
            nextFocus = focusAddressLine2,
            autofillType = AutofillType.AddressStreet,
            onValueUpdated = viewModel::updateAddressLine1
        )
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusAddressLine2)
                .testTag("addressLine2Input"),
            value = uiState.addressLine2,
            label = stringResource(R.string.label_address_line_2),
            nextFocus = focusCity,
            onValueUpdated = viewModel::updateAddressLine2
        )
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusCity)
                .testTag("cityInput"),
            value = uiState.city,
            label = stringResource(R.string.label_city),
            nextFocus = focusState,
            autofillType = AutofillType.AddressLocality,
            onValueUpdated = viewModel::updateCity
        )
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusState)
                .testTag("stateInput"),
            value = uiState.state,
            label = stringResource(R.string.label_state),
            nextFocus = focusPostalCode,
            autofillType = AutofillType.AddressRegion,
            onValueUpdated = viewModel::updateState
        )
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusPostalCode)
                .testTag("postalCodeInput"),
            value = uiState.postalCode,
            label = stringResource(R.string.label_postal_code),
            autofillType = AutofillType.PostalCode,
            onValueUpdated = viewModel::updatePostalCode
        )
        // Country auto-complete dropdown selection
        CountryInputAutoComplete(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("countryInput"),
            selectedItem = uiState.country,
            autofillType = AutofillType.AddressCountry,
            onCountrySelected = viewModel::updateCountry
        )
        // Use a LaunchedEffect to delay the callback invocation
        LaunchedEffect(uiState) {
            delay(MobileSDKConstants.General.DEBOUNCE_DELAY) // Delay by 500 milliseconds
            onAddressUpdated(uiState)
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewManualAddress() {
    SdkTheme {
        ManualAddress(onAddressUpdated = {})
    }
}

@PreviewLightDark
@Composable
internal fun PreviewManualAddressWithDefault() {
    val address = BillingAddress(
        addressLine1 = "1 Park Avenue",
        city = "Manchester",
        state = "Greater Manchester",
        postalCode = "M11 5MW",
        country = "United Kingdom"
    )
    SdkTheme {
        ManualAddress(savedAddress = address, onAddressUpdated = {})
    }
}