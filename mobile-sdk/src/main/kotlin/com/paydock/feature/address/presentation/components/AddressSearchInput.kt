package com.paydock.feature.address.presentation.components

import android.location.Address
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.search.SearchTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.address.presentation.viewmodels.AddressSearchViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A composable that provides an address search input field with a dropdown menu for selecting addresses.
 *
 * @param modifier The modifier to apply to the composable.
 * @param viewModel The ViewModel responsible for managing address search.
 * @param onAddressSelected Callback function to handle the selected address.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun AddressSearchInput(
    modifier: Modifier = Modifier,
    viewModel: AddressSearchViewModel = koinViewModel(),
    onAddressSelected: (Address) -> Unit
) {
    val searchResults by viewModel.searchResult.collectAsState()

    SearchTextField(
        modifier = modifier.testTag("addressSearch"),
        label = stringResource(R.string.label_search_for_your_address),
        autofillType = AutofillType.AddressStreet,
        noResultsFoundLabel = stringResource(R.string.label_no_address_found),
        viewModel = viewModel,
        onItemSelected = { item ->
            val address = searchResults.find { it.getAddressLine(0) == item }
            address?.let { onAddressSelected(it) }
        }
    )
}

@LightDarkPreview
@Composable
private fun PreviewAddressSearchInput() {
    SdkTheme {
        AddressSearchInput(onAddressSelected = {})
    }
}