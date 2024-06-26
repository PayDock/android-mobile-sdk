package com.paydock.feature.address.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.designsystems.components.InputValidIcon
import com.paydock.designsystems.components.search.SearchTextField
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable for displaying a country autocomplete input.
 *
 * @param modifier Modifier to apply to the composable.
 * @param viewModel ViewModel for country autocomplete functionality.
 * @param selectedItem The pre-selected item to be displayed initially.
 * @param onCountrySelected Callback when a country is selected.
 */
@Composable
internal fun CountryInputAutoComplete(
    modifier: Modifier = Modifier,
    viewModel: CountryAutoCompleteViewModel = koinViewModel(),
    selectedItem: String = "",
    onCountrySelected: (String) -> Unit
) {
    SearchTextField(
        modifier = modifier.testTag("countrySearch"),
        label = stringResource(R.string.label_country),
        trailingIcon = {
            if (selectedItem.isNotBlank()) {
                InputValidIcon()
            } else {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.ArrowDropDown),
                    contentDescription = stringResource(id = R.string.content_desc_dropdown_arrow),
                )
            }
        },
        selectedItem = selectedItem,
        viewModel = viewModel,
        onItemSelected = {
            onCountrySelected(it as String)
        }
    )
}