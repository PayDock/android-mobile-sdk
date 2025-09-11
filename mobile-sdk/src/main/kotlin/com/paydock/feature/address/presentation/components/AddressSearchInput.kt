package com.paydock.feature.address.presentation.components

import android.location.Address
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.designsystems.components.search.SearchDropdownAppearance
import com.paydock.designsystems.components.search.SearchDropdownAppearanceDefaults
import com.paydock.designsystems.components.search.SearchTextField
import com.paydock.feature.address.presentation.viewmodels.AddressSearchViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A composable that provides an address search input field with a dropdown menu for selecting addresses.
 *
 * @param modifier The modifier to apply to the composable.
 * @param appearance The appearance configuration for the search dropdown.
 * @param viewModel The ViewModel responsible for managing address search.
 * @param onAddressSelected Callback function to handle the selected address.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun AddressSearchInput(
    modifier: Modifier = Modifier,
    appearance: SearchDropdownAppearance = SearchDropdownAppearanceDefaults.appearance(),
    viewModel: AddressSearchViewModel = koinViewModel(),
    onAddressSelected: (Address) -> Unit
) {
    SearchTextField(
        modifier = modifier.testTag("addressSearch"),
        appearance = appearance,
        label = stringResource(R.string.label_search_for_your_address),
        autofillType = ContentType.AddressStreet,
        noResultsFoundLabel = stringResource(R.string.label_no_address_found),
        viewModel = viewModel,
        selectedItemProp = null,
        isMandatory = false,
        onSelectionChanged = { selectedObject ->
            if (selectedObject is Address) {
                // A valid Address object was selected from the SearchTextField's dropdown
                onAddressSelected(selectedObject)
                // Clear the search text field in the ViewModel after selection.
                // This will make the searchText in SearchTextField empty.
                viewModel.onSearchTextChange("")
            }
        }
    )
}