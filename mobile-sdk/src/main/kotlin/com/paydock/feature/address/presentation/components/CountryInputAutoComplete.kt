package com.paydock.feature.address.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
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
 * @param autofillType An optional [AutofillType] indicating the type of data that can be
 * autofilled for this input field (e.g., address, postal code). If provided, it enables
 * autofill support.
 * @param onCountrySelected Callback when a country is selected.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CountryInputAutoComplete(
    modifier: Modifier = Modifier,
    viewModel: CountryAutoCompleteViewModel = koinViewModel(),
    autofillType: AutofillType? = null,
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
        autofillType = autofillType,
        selectedItem = selectedItem,
        viewModel = viewModel,
        onItemSelected = {
            onCountrySelected(it as String)
        }
    )
}