package com.paydock.feature.address.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.paydock.R
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.designsystems.components.search.SearchDropdownAppearance
import com.paydock.designsystems.components.search.SearchDropdownAppearanceDefaults
import com.paydock.designsystems.core.WidgetDefaults
import com.paydock.feature.address.presentation.state.AddressDetailsFormState

/**
 * Displays a manual address entry form with input fields for the user to update their address.
 *
 * @param modifier [Modifier] to apply customizations to the layout of this component.
 * @param addressInputState The current state of the address input fields, represented by [AddressDetailsFormState].
 * This state includes values for address line 1, address line 2, city, state, postal code, and country.
 * @param onAddressLine1Change A callback invoked when the value of the Address Line 1 input field changes.
 * The new value is passed as a [String].
 * @param onAddressLine2Change A callback invoked when the value of the Address Line 2 input field changes.
 * The new value is passed as a [String].
 * @param onCityChange A callback invoked when the value of the City input field changes.
 * The new value is passed as a [String].
 * @param onStateChange A callback invoked when the value of the State input field changes.
 * The new value is passed as a [String].
 * @param onPostalCodeChange A callback invoked when the value of the Postal Code input field changes.
 * The new value is passed as a [String].
 * @param onCountryChange A callback invoked when the value of the Country input field changes.
 * The new value is passed as a [String].
 * @param verticalSpacing The vertical spacing between input fields. Defaults to [WidgetDefaults.Spacing].
 * @param textFieldAppearance The appearance style for the text input fields. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param searchAppearance The appearance style for the search dropdown (country selection).
 * Defaults to [SearchDropdownAppearanceDefaults.appearance].
 *
 * This function provides:
 * - Input fields for address components such as Address Line 1, Address Line 2, City, State, Postal Code, and Country.
 * - Autofill support for each field to enhance user experience.
 * - Keyboard navigation between fields using [FocusRequester].
 *
 * Notes:
 * - Autofill types are configured for each input field to match their specific purpose (e.g., street, locality).
 * - A [CountryInputAutoComplete] is used for country selection with a dropdown.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongMethod")
@Composable
internal fun ManualAddress(
    modifier: Modifier = Modifier,
    addressInputState: AddressDetailsFormState,
    onAddressLine1Change: (String) -> Unit,
    onAddressLine2Change: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    verticalSpacing: Dp = WidgetDefaults.Spacing,
    textFieldAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    searchAppearance: SearchDropdownAppearance = SearchDropdownAppearanceDefaults.appearance(),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        val focusAddressLine2 = remember { FocusRequester() }
        val focusCity = remember { FocusRequester() }
        val focusState = remember { FocusRequester() }
        val focusPostalCode = remember { FocusRequester() }

        // Address input fields
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("addressLine1Input"),
            appearance = textFieldAppearance,
            value = addressInputState.addressLine1,
            label = stringResource(R.string.label_address_line_1),
            nextFocus = focusAddressLine2,
            autofillType = AutofillType.AddressStreet,
            onValueUpdated = onAddressLine1Change
        )
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusAddressLine2)
                .testTag("addressLine2Input"),
            appearance = textFieldAppearance,
            value = addressInputState.addressLine2,
            label = stringResource(R.string.label_address_line_2),
            nextFocus = focusCity,
            onValueUpdated = onAddressLine2Change
        )
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusCity)
                .testTag("cityInput"),
            appearance = textFieldAppearance,
            value = addressInputState.city,
            label = stringResource(R.string.label_city),
            nextFocus = focusState,
            autofillType = AutofillType.AddressLocality,
            onValueUpdated = onCityChange
        )
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusState)
                .testTag("stateInput"),
            appearance = textFieldAppearance,
            value = addressInputState.state,
            label = stringResource(R.string.label_state),
            nextFocus = focusPostalCode,
            autofillType = AutofillType.AddressRegion,
            onValueUpdated = onStateChange
        )
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusPostalCode)
                .testTag("postalCodeInput"),
            appearance = textFieldAppearance,
            value = addressInputState.postalCode,
            label = stringResource(R.string.label_postal_code),
            autofillType = AutofillType.PostalCode,
            onValueUpdated = onPostalCodeChange
        )
        // Country auto-complete dropdown selection
        CountryInputAutoComplete(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("countryInput"),
            appearance = searchAppearance,
            selectedItem = addressInputState.country,
            autofillType = AutofillType.AddressCountry,
            onCountrySelected = onCountryChange
        )
    }
}