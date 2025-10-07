package com.paydock.feature.address.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.designsystems.components.search.SearchDropdownAppearance
import com.paydock.designsystems.components.search.SearchDropdownAppearanceDefaults
import com.paydock.designsystems.components.search.SearchTextField
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable for displaying a country autocomplete input.
 *
 * @param modifier Modifier to apply to the composable.
 * @param appearance Appearance configuration for the search dropdown.
 * @param viewModel ViewModel for country autocomplete functionality.
 * @param autofillType An optional [ContentType] indicating the type of data that can be
 * autofilled for this input field (e.g., address, postal code). If provided, it enables
 * autofill support.
 * @param currentCountryValue The currently selected country value. This is used to initialize
 * the input and to update the displayed icon.
 * @param onCountryConfirmed Callback function invoked when a country is confirmed or cleared.
 * It receives the selected country string, or null if the selection is cleared.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CountryInputAutoComplete(
    modifier: Modifier = Modifier,
    appearance: SearchDropdownAppearance = SearchDropdownAppearanceDefaults.appearance(),
    viewModel: CountryAutoCompleteViewModel = koinViewModel(),
    autofillType: ContentType? = null,
    currentCountryValue: String,
    onCountryConfirmed: (String?) -> Unit
) {
    var confirmedSelectionForIcon by remember(currentCountryValue) {
        mutableStateOf(currentCountryValue.ifBlank { null })
    }

    SearchTextField(
        modifier = modifier.testTag("countrySearch"),
        appearance = appearance,
        label = stringResource(R.string.label_country),
        autofillType = autofillType,
        selectedItemProp = confirmedSelectionForIcon,
        viewModel = viewModel,
        onSelectionChanged = { selectedObject ->
            if (selectedObject is String) {
                // A valid country string was selected from the dropdown or matched.
                confirmedSelectionForIcon = selectedObject
                onCountryConfirmed(selectedObject)

                viewModel.onSearchTextChange(selectedObject)
            } else if (selectedObject == null) {
                confirmedSelectionForIcon = null
                onCountryConfirmed(null)
                // DO NOT clear viewModel.searchText here. SearchTextField handles its own text.
            }
        }
    )
}